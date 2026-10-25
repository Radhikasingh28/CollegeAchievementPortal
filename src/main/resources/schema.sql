CREATE TABLE IF NOT EXISTS students (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 full_name VARCHAR(120) NOT NULL,
 roll_number VARCHAR(40) NOT NULL UNIQUE,
 department VARCHAR(80) NOT NULL,
 year_of_study INT NOT NULL DEFAULT 3
);
CREATE TABLE IF NOT EXISTS achievements (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 student_id BIGINT NOT NULL,
 title VARCHAR(180) NOT NULL,
 category VARCHAR(60) NOT NULL,
 achievement_level VARCHAR(40) NOT NULL DEFAULT 'College',
 description TEXT,
 proof_url VARCHAR(500),
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 points INT NOT NULL DEFAULT 0,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_achievement_student FOREIGN KEY (student_id) REFERENCES students(id)
);
INSERT IGNORE INTO students (id,full_name,roll_number,department,year_of_study) VALUES
(1,'Aarav Sharma','NIET2024001','CSE',3),
(2,'Priya Verma','NIET2024002','CSE',3),
(3,'Kabir Singh','NIET2024003','IT',3),
(4,'Ananya Gupta','NIET2024004','CSE-AI',3),
(5,'Rohan Kumar','NIET2024005','ECE',3);
INSERT INTO achievements (student_id,title,category,achievement_level,description,status,points)
SELECT 1,'College Hackathon Finalist','Hackathon','College','Sample demo record','VERIFIED',10
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE student_id=1 AND title='College Hackathon Finalist');
INSERT INTO achievements (student_id,title,category,achievement_level,description,status,points)
SELECT 2,'Java Programming Certificate','Certification','Online','Sample demo record','VERIFIED',5
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE student_id=2 AND title='Java Programming Certificate');
INSERT INTO achievements (student_id,title,category,achievement_level,description,status,points)
SELECT 3,'Smart Campus Project','Project','College','Sample demo record','PENDING',0
WHERE NOT EXISTS (SELECT 1 FROM achievements WHERE student_id=3 AND title='Smart Campus Project');

CREATE TABLE IF NOT EXISTS portal_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    student_id BIGINT NULL,
    CONSTRAINT fk_portal_user_student
        FOREIGN KEY (student_id) REFERENCES students(id)
);
