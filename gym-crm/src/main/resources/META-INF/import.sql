INSERT INTO training_types (training_type_name) VALUES ('Cardio');
INSERT INTO training_types (training_type_name) VALUES ('Strength Training');
INSERT INTO training_types (training_type_name) VALUES ('Yoga');
INSERT INTO training_types (training_type_name) VALUES ('Pilates');
INSERT INTO training_types (training_type_name) VALUES ('CrossFit');
INSERT INTO training_types (training_type_name) VALUES ('HIIT');
INSERT INTO training_types (training_type_name) VALUES ('Cycling');
INSERT INTO training_types (training_type_name) VALUES ('Zumba');
INSERT INTO training_types (training_type_name) VALUES ('Boxing');
INSERT INTO training_types (training_type_name) VALUES ('Swimming');

-- Inserting Users (Trainees and Trainers)
INSERT INTO users (id, first_name, last_name, username, password, is_active, role) VALUES (100, 'John', 'Doe', 'john.doe', '$2a$10$WPCzhc.jFB8vQ7XbAcTRIeRvSLSJxaApl6TnO6QwTvRki8U96ffkq', true, 'TRAINEE');
INSERT INTO users (id, first_name, last_name, username, password, is_active, role) VALUES (200, 'Jane', 'Smith', 'jane.smith', '$2a$10$fS5w008C5r6DEzpHE3N2nOO4HFRP3/epOnzCvZuyE1FBte0lIFPJm', true, 'TRAINEE');
INSERT INTO users (id, first_name, last_name, username, password, is_active, role) VALUES (300, 'Mike', 'Johnson', 'mike.johnson', '$2a$10$6ecLRpUavI37PVPzeFQNVeqtbPsKXHKh.9gzLOSK5OLZZc731AM5G', true, 'TRAINEE');
INSERT INTO users (id, first_name, last_name, username, password, is_active, role) VALUES (400, 'Emma', 'Brown', 'emma.brown', '$2a$10$9vR5m5XuvVmj5qNmwR3/yOhqPaVOPLitmkDfFs3v/nZpWZqnjWroq', true, 'TRAINER');
INSERT INTO users (id, first_name, last_name, username, password, is_active, role) VALUES (500, 'David', 'Williams', 'david.williams', '$2a$10$YHXbGVAfyBBP9lUmyOq5aOOwrJ7vfPfCnEgI3Oqje8S32NBiEc.oW', true, 'TRAINER');
INSERT INTO users (id, first_name, last_name, username, password, is_active, role) VALUES (600, 'Sarah', 'Miller', 'sarah.miller', '$2a$10$eU47shKNsUuq1dLtLub0/.jUwwJbHbEZVe0WyU4bOMoeBVF2tceNW', true, 'TRAINER');

-- Inserting Trainees
INSERT INTO trainees (id, date_of_birth, address, user_id) VALUES (100, '1995-06-15', '123 Main St, City A', 100);
INSERT INTO trainees (id, date_of_birth, address, user_id) VALUES (200, '1998-09-22', '456 Oak St, City B', 200);
INSERT INTO trainees (id, date_of_birth, address, user_id) VALUES (300, '2000-01-10', '789 Pine St, City C', 300);

-- Inserting Trainers
INSERT INTO trainers (id, specialization_id, user_id) VALUES (100, (SELECT id FROM training_types WHERE training_type_name = 'Cardio'), 400);
INSERT INTO trainers (id, specialization_id, user_id) VALUES (200, (SELECT id FROM training_types WHERE training_type_name = 'Strength Training'), 500);
INSERT INTO trainers (id, specialization_id, user_id) VALUES (300, (SELECT id FROM training_types WHERE training_type_name = 'Yoga'), 600);

-- Inserting Trainee-Trainer Relationships (Who trains with whom)
INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (100, 100);
INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (200, 200);
INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (300, 300);

-- Inserting Trainings
INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (100, 100, 100, 'Morning Cardio', (SELECT id FROM training_types WHERE training_type_name = 'Cardio'), '2024-06-10', 60);
INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (200, 200, 200, 'Strength Training Session', (SELECT id FROM training_types WHERE training_type_name = 'Strength Training'), '2024-06-12', 90);
INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (300, 300, 300, 'Yoga Class', (SELECT id FROM training_types WHERE training_type_name = 'Yoga'), '2024-06-15', 75);
INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (400, 100, 200, 'Weight Lifting Basics', (SELECT id FROM training_types WHERE training_type_name = 'Strength Training'), '2024-06-18', 60);
INSERT INTO trainings (id, trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES (500, 200, 300, 'Advanced Yoga', (SELECT id FROM training_types WHERE training_type_name = 'Yoga'), '2024-06-20', 90);
