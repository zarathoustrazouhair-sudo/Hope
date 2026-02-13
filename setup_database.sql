-- Users Table
CREATE TABLE public.users (
    id UUID PRIMARY KEY,
    email TEXT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    role TEXT NOT NULL, -- 'SYNDIC', 'RESIDENT', 'CONCIERGE'
    building TEXT NOT NULL,
    apartment_number TEXT NOT NULL,
    pin_hash TEXT, -- Stored locally usually, but syncable if encrypted
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Incidents Table
CREATE TABLE public.incidents (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    photo_url TEXT,
    status TEXT NOT NULL, -- 'OPEN', 'IN_PROGRESS', 'RESOLVED'
    priority TEXT NOT NULL,
    user_id UUID REFERENCES public.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Blog Posts Table (Phase 8)
CREATE TABLE public.blog_posts (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT NOT NULL, -- 'Annonce', 'Event'
    author_id UUID REFERENCES public.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- RLS Policies (Simplified for MVP)
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.incidents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blog_posts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Public Read Users" ON public.users FOR SELECT USING (true);
CREATE POLICY "Public Read Incidents" ON public.incidents FOR SELECT USING (true);
CREATE POLICY "Public Read Blog" ON public.blog_posts FOR SELECT USING (true);

-- Allow authenticated users to insert (MVP: Open access for authenticated)
CREATE POLICY "Auth Insert Incidents" ON public.incidents FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Syndic Insert Blog" ON public.blog_posts FOR INSERT WITH CHECK (true); -- Ideally restrict to role='SYNDIC'
