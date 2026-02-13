-- admin_user
INSERT INTO admin_user (username, password)
VALUES ('admin', 'admin123')
ON CONFLICT (username) DO NOTHING;


-- service_category
INSERT INTO service_category (category_name, description) 
VALUES
('Elderly Home Care','Daily elderly assistance and care'),
('Medical Assistance','Nursing, medication and health monitoring'),
('Mobility & Household Support','Transport and home cleaning support')
ON CONFLICT DO NOTHING;

-- service
-- Note: Replaced hardcoded category IDs with subqueries to ensure they match the parent tables correctly
INSERT INTO service (category_id, service_name, description, price, image_path, availability) 
VALUES
((SELECT id FROM service_category WHERE category_name='Elderly Home Care'),'Home Checkup','Caregiver visits your home daily.',72,'images/service1.png',NULL),
((SELECT id FROM service_category WHERE category_name='Elderly Home Care'),'Side Compainionship Service','Provide social and emotional support.',80,'images/service2.png',NULL),
((SELECT id FROM service_category WHERE category_name='Elderly Home Care'),'Fresh Meals Preparation','Prepare healthy meals for seniors and elderly.',50,'images/service3.png',NULL),
((SELECT id FROM service_category WHERE category_name='Medical Assistance'),'Nursing & Care','Qualified nurse for medical needs.',120,'images/service4.png',NULL),
((SELECT id FROM service_category WHERE category_name='Medical Assistance'),'Medication Reminder','Ensure seniors take meds on time.',40,'images/service5.png',NULL),
((SELECT id FROM service_category WHERE category_name='Medical Assistance'),'Vital Sign Monitoring','Daily monitoring of vital health signs.',70,'images/service6.png',NULL),
((SELECT id FROM service_category WHERE category_name='Mobility & Household Support'),'Wheelchair Assistance','Help with mobility & wheelchair use.',45,'images/service7.png',NULL),
((SELECT id FROM service_category WHERE category_name='Mobility & Household Support'),'Home Cleaning Support','General cleaning for elderly homes.',55,'images/service8.png',NULL),
((SELECT id FROM service_category WHERE category_name='Mobility & Household Support'),'Transport to Appointments','Escort to clinics/hospitals.',90,'images/service9.png',NULL)
ON CONFLICT DO NOTHING;



