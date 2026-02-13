-- =========================
-- admin_user
-- =========================
INSERT INTO admin_user (username, password)
SELECT 'admin', 'admin123'
WHERE NOT EXISTS (
    SELECT 1 FROM admin_user WHERE username = 'admin'
);

-- =========================
-- service_category
-- =========================
INSERT INTO service_category (category_name, description)
SELECT 'Elderly Home Care','Daily elderly assistance and care'
WHERE NOT EXISTS (
    SELECT 1 FROM service_category WHERE category_name='Elderly Home Care'
);

INSERT INTO service_category (category_name, description)
SELECT 'Medical Assistance','Nursing, medication and health monitoring'
WHERE NOT EXISTS (
    SELECT 1 FROM service_category WHERE category_name='Medical Assistance'
);

INSERT INTO service_category (category_name, description)
SELECT 'Mobility & Household Support','Transport and home cleaning support'
WHERE NOT EXISTS (
    SELECT 1 FROM service_category WHERE category_name='Mobility & Household Support'
);

-- =========================
-- service
-- =========================
INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Elderly Home Care'),
    'Home Checkup','Caregiver visits your home daily.',72,'images/service1.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Home Checkup'
);

INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Elderly Home Care'),
    'Side Compainionship Service','Provide social and emotional support.',80,'images/service2.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Side Compainionship Service'
);

INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Elderly Home Care'),
    'Fresh Meals Preparation','Prepare healthy meals for seniors and elderly.',50,'images/service3.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Fresh Meals Preparation'
);

INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Medical Assistance'),
    'Nursing & Care','Qualified nurse for medical needs.',120,'images/service4.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Nursing & Care'
);

INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Medical Assistance'),
    'Medication Reminder','Ensure seniors take meds on time.',40,'images/service5.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Medication Reminder'
);

INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Medical Assistance'),
    'Vital Sign Monitoring','Daily monitoring of vital health signs.',70,'images/service6.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Vital Sign Monitoring'
);

INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Mobility & Household Support'),
    'Wheelchair Assistance','Help with mobility & wheelchair use.',45,'images/service7.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Wheelchair Assistance'
);

INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Mobility & Household Support'),
    'Home Cleaning Support','General cleaning for elderly homes.',55,'images/service8.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Home Cleaning Support'
);

INSERT INTO service (category_id, service_name, description, price, image_path, availability)
SELECT 
    (SELECT id FROM service_category WHERE category_name='Mobility & Household Support'),
    'Transport to Appointments','Escort to clinics/hospitals.',90,'images/service9.png',NULL
WHERE NOT EXISTS (
    SELECT 1 FROM service WHERE service_name='Transport to Appointments'
);
