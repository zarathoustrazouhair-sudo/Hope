-- 1. Create Enumerations
CREATE TYPE user_role AS ENUM ('SYNDIC', 'ADJOINT', 'CONCIERGE', 'RESIDENT');
CREATE TYPE incident_status AS ENUM ('OPEN', 'IN_PROGRESS', 'RESOLVED');
CREATE TYPE incident_priority AS ENUM ('LOW', 'NORMAL', 'URGENT');
CREATE TYPE transaction_type AS ENUM ('COTISATION', 'PAIEMENT', 'DEPENSE');

-- 2. Create Profiles Table (Extension of auth.users)
-- Note: id usually references auth.users(id), but for seed data without users,
-- we might need to relax this constraint or ensure users exist.
-- For this script, we assume the constraint is strict for production,
-- but for initial seed, we might need a workaround if users don't exist.
-- To allow seed data insertion without existing auth users, we would need to
-- disable the FK constraint or insert dummy users into auth.users (which requires admin privs).
-- STRATEGY: We will keep the FK constraint. The seed data below assumes
-- that corresponding users will be created in auth.users with these specific UUIDs
-- OR the constraint is temporarily disabled for the seed.
-- FOR THIS DELIVERABLE: We will define the table strictly.
-- The seed data will be provided as INSERT statements that might fail if users don't exist,
-- so user must create these users or we use a procedural approach.
CREATE TABLE public.profiles (
  id UUID REFERENCES auth.users ON DELETE CASCADE PRIMARY KEY,
  role user_role NOT NULL DEFAULT 'RESIDENT',
  email TEXT NOT NULL,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  phone_number TEXT,
  cin TEXT,
  mandate_start_date DATE,
  building TEXT NOT NULL DEFAULT 'Amandier B',
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
  residence_name TEXT NOT NULL DEFAULT 'Amandier B',
  logo_url TEXT,
  monthly_fee NUMERIC NOT NULL DEFAULT 250.00,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Create Transactions Table (Financial Core)
CREATE TABLE public.transactions (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL, -- Nullable for global expenses
  amount NUMERIC(10, 2) NOT NULL, -- Positive (Payment), Negative (Debit/Expense)
  type transaction_type NOT NULL,
  label TEXT NOT NULL,
  date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. Enable Row Level Security (RLS)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.incidents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.residence_config ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;

-- 7. Define RLS Policies

-- Profiles:
CREATE POLICY "Users view own profile" ON public.profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Syndic view all profiles" ON public.profiles FOR SELECT USING ((SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('SYNDIC', 'ADJOINT'));
CREATE POLICY "Users update own profile" ON public.profiles FOR UPDATE USING (auth.uid() = id);

-- Incidents:
CREATE POLICY "Syndic view all incidents" ON public.incidents FOR SELECT USING ((SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('SYNDIC', 'ADJOINT'));
CREATE POLICY "Residents view own incidents" ON public.incidents FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "Residents create incidents" ON public.incidents FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users update own incidents" ON public.incidents FOR UPDATE USING (auth.uid() = user_id);
CREATE POLICY "Syndic updates any incident" ON public.incidents FOR UPDATE USING ((SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('SYNDIC', 'ADJOINT'));

-- Transactions:
-- Residents see their own transactions (History)
CREATE POLICY "Residents view own transactions" ON public.transactions FOR SELECT USING (auth.uid() = user_id);
-- Syndic sees ALL transactions (Financial Dashboard)
CREATE POLICY "Syndic view all transactions" ON public.transactions FOR SELECT USING ((SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('SYNDIC', 'ADJOINT'));
-- Only Syndic can create transactions (or system via admin function)
CREATE POLICY "Syndic create transactions" ON public.transactions FOR INSERT WITH CHECK ((SELECT role FROM public.profiles WHERE id = auth.uid()) IN ('SYNDIC', 'ADJOINT'));

-- Residence Config:
CREATE POLICY "Everyone view config" ON public.residence_config FOR SELECT USING (true);
CREATE POLICY "Syndic update config" ON public.residence_config FOR UPDATE USING ((SELECT role FROM public.profiles WHERE id = auth.uid()) = 'SYNDIC');

-- 8. Triggers for updated_at
CREATE OR REPLACE FUNCTION update_modified_column() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_profiles_modtime BEFORE UPDATE ON public.profiles FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
CREATE TRIGGER update_incidents_modtime BEFORE UPDATE ON public.incidents FOR EACH ROW EXECUTE PROCEDURE update_modified_column();
CREATE TRIGGER update_residence_config_modtime BEFORE UPDATE ON public.residence_config FOR EACH ROW EXECUTE PROCEDURE update_modified_column();

-- 9. Trigger to create Profile on User Sign Up
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
    COALESCE(NEW.raw_user_meta_data->>'building', 'Amandier B'),
    COALESCE(NEW.raw_user_meta_data->>'apartment_number', '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user();

-- 10. SEED DATA (Residents AP1-AP15)
-- NOTE: These inserts will fail if the UUIDs don't exist in auth.users due to FK constraint.
-- In a real scenario, you would create users via the Auth API first.
-- For the purpose of this script, we are providing the logical data structure.
-- The UUIDs here are placeholders. In production, these should be the actual User IDs from Supabase Auth.

/*
INSERT INTO public.profiles (id, email, first_name, last_name, role, building, apartment_number) VALUES
('00000000-0000-0000-0000-000000000001', 'ap1@amandier.com', 'Adnan', 'Ayazi', 'RESIDENT', 'Amandier B', '1'),
('00000000-0000-0000-0000-000000000002', 'ap2@amandier.com', 'Fatima', 'Dehbi', 'RESIDENT', 'Amandier B', '2'),
('00000000-0000-0000-0000-000000000003', 'ap3@amandier.com', 'Nora', 'Mouktadi', 'RESIDENT', 'Amandier B', '3'),
('00000000-0000-0000-0000-000000000004', 'ap4@amandier.com', 'Jalila', 'Annan', 'RESIDENT', 'Amandier B', '4'),
('00000000-0000-0000-0000-000000000005', 'ap5@amandier.com', 'Yahya', 'Sbai', 'RESIDENT', 'Amandier B', '5'),
('00000000-0000-0000-0000-000000000006', 'ap6@amandier.com', 'Yasmine', 'Boukherssa', 'RESIDENT', 'Amandier B', '6'),
('00000000-0000-0000-0000-000000000007', 'ap7@amandier.com', 'Jalal', 'Liassini', 'RESIDENT', 'Amandier B', '7'),
('00000000-0000-0000-0000-000000000008', 'ap8@amandier.com', 'Abdelati', 'Kenbouchi', 'SYNDIC', 'Amandier B', '8'),
('00000000-0000-0000-0000-000000000009', 'ap9@amandier.com', 'Sbaili', 'Marwa', 'RESIDENT', 'Amandier B', '9'),
('00000000-0000-0000-0000-000000000010', 'ap10@amandier.com', 'Bessam', 'Halil', 'RESIDENT', 'Amandier B', '10'),
('00000000-0000-0000-0000-000000000011', 'ap11@amandier.com', 'Adil', 'Rahil', 'RESIDENT', 'Amandier B', '11'),
('00000000-0000-0000-0000-000000000012', 'ap12@amandier.com', 'Mohsine', 'Arif', 'RESIDENT', 'Amandier B', '12'),
('00000000-0000-0000-0000-000000000013', 'ap13@amandier.com', 'Asmaa', 'Oualad', 'RESIDENT', 'Amandier B', '13'),
('00000000-0000-0000-0000-000000000014', 'ap14@amandier.com', 'Naoual', 'Fidar', 'RESIDENT', 'Amandier B', '14'),
('00000000-0000-0000-0000-000000000015', 'ap15@amandier.com', 'Kawtar', 'Es-Saidi', 'RESIDENT', 'Amandier B', '15');
*/
