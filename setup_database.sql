-- 1. Create Enumerations
CREATE TYPE user_role AS ENUM ('SYNDIC', 'ADJOINT', 'CONCIERGE', 'RESIDENT');
CREATE TYPE incident_status AS ENUM ('OPEN', 'IN_PROGRESS', 'RESOLVED');
CREATE TYPE incident_priority AS ENUM ('LOW', 'NORMAL', 'URGENT');

-- 2. Create Profiles Table (Extension of auth.users)
CREATE TABLE public.profiles (
  id UUID REFERENCES auth.users ON DELETE CASCADE PRIMARY KEY,
  role user_role NOT NULL DEFAULT 'RESIDENT',
  email TEXT NOT NULL,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  phone_number TEXT,
  cin TEXT, -- Specific to CONCIERGE
  mandate_start_date DATE, -- Specific to SYNDIC
  building TEXT NOT NULL,
  apartment_number TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. Create Incidents Table
CREATE TABLE public.incidents (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT NOT NULL,
  photo_url TEXT,
  status incident_status NOT NULL DEFAULT 'OPEN',
  priority incident_priority NOT NULL DEFAULT 'NORMAL',
  user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Create Residence Config Table (Single Row)
CREATE TABLE public.residence_config (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  residence_name TEXT NOT NULL,
  logo_url TEXT,
  monthly_fee NUMERIC NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Enable Row Level Security (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.incidents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.residence_config ENABLE ROW LEVEL SECURITY;

-- 6. Define RLS Policies

-- Profiles:
-- Users can view their own profile.
CREATE POLICY "Users can view own profile" ON public.profiles
  FOR SELECT USING (auth.uid() = id);

-- Syndic and Adjoint can view all profiles.
CREATE POLICY "Syndic and Adjoint view all profiles" ON public.profiles
  FOR SELECT USING (
    (SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('SYNDIC', 'ADJOINT')
  );

-- Users can update their own profile.
CREATE POLICY "Users can update own profile" ON public.profiles
  FOR UPDATE USING (auth.uid() = id);

-- Incidents:
-- Syndic and Adjoint can view all incidents.
CREATE POLICY "Syndic and Adjoint view all incidents" ON public.incidents
  FOR SELECT USING (
    (SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('SYNDIC', 'ADJOINT')
  );

-- Residents can view their own incidents.
CREATE POLICY "Residents can view own incidents" ON public.incidents
  FOR SELECT USING (auth.uid() = user_id);

-- Residents can create incidents.
CREATE POLICY "Residents can create incidents" ON public.incidents
  FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Only Syndic/Adjoint can update status/priority (or users edit description).
-- For MVP simplification: Users can update their own incidents, Syndic can update any.
CREATE POLICY "Users update own incidents" ON public.incidents
  FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Syndic updates any incident" ON public.incidents
  FOR UPDATE USING (
    (SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('SYNDIC', 'ADJOINT')
  );

-- Residence Config:
-- Everyone can view residence config.
CREATE POLICY "Everyone can view residence config" ON public.residence_config
  FOR SELECT USING (true);

-- Only Syndic can update residence config.
CREATE POLICY "Syndic can update residence config" ON public.residence_config
  FOR UPDATE USING (
    (SELECT role FROM public.profiles WHERE id = auth.uid()) = 'SYNDIC'
  );

-- 7. Triggers for updated_at
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_profiles_modtime BEFORE UPDATE ON public.profiles FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
CREATE TRIGGER update_incidents_modtime BEFORE UPDATE ON public.incidents FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
CREATE TRIGGER update_residence_config_modtime BEFORE UPDATE ON public.residence_config FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- 8. Trigger to create Profile on User Sign Up (Optional but recommended)
-- 8. Trigger to create Profile on User Sign Up
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, email, first_name, last_name, role, building, apartment_number)
  VALUES (
    NEW.id,
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'first_name', ''),
    COALESCE(NEW.raw_user_meta_data->>'last_name', ''),
    COALESCE((NEW.raw_user_meta_data->>'role')::user_role, 'RESIDENT'),
    COALESCE(NEW.raw_user_meta_data->>'building', ''),
    COALESCE(NEW.raw_user_meta_data->>'apartment_number', '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- 9. Storage Buckets (via SQL is limited, usually done via Dashboard, but policy is key)
-- Access policies for Storage would need to be defined in the Storage section of Supabase.
-- This script focuses on the Database schema.
