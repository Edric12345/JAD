-- MySQL dump 10.13  Distrib 9.5.0, for macos15.7 (arm64)
--
-- Host: localhost    Database: db1
-- ------------------------------------------------------
-- Server version	9.0.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_user`
--

DROP TABLE IF EXISTS `admin_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_user`
--

LOCK TABLES `admin_user` WRITE;
/*!40000 ALTER TABLE `admin_user` DISABLE KEYS */;
INSERT INTO `admin_user` VALUES (1,'admin','admin123');
/*!40000 ALTER TABLE `admin_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `booking_details`
--

DROP TABLE IF EXISTS `booking_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `booking_details` (
  `id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `service_id` int NOT NULL,
  `quantity` int DEFAULT '1',
  `subtotal` double NOT NULL,
  `price_at_booking` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `booking_id` (`booking_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `booking_details_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  CONSTRAINT `booking_details_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `booking_details`
--

LOCK TABLES `booking_details` WRITE;
/*!40000 ALTER TABLE `booking_details` DISABLE KEYS */;
INSERT INTO `booking_details` VALUES (1,1,1,1,80,0),(2,2,5,1,40,0),(3,3,7,1,45,0),(4,4,4,1,120,0),(5,5,4,0,0,120),(6,6,9,0,0,90),(7,7,3,0,0,50),(8,8,3,0,0,50),(9,9,3,0,0,50),(10,10,4,0,120,120),(11,11,4,0,120,120),(12,12,4,0,0,120),(13,13,4,0,0,120),(14,14,3,0,0,50),(15,15,3,0,0,50),(16,16,4,0,0,120),(17,17,5,0,0,40),(18,18,3,0,0,50),(19,19,9,0,0,90),(22,22,3,0,0,50),(23,23,3,0,0,50),(27,27,6,0,0,70),(28,28,10,0,0,52),(36,36,3,0,50,50),(37,37,6,0,0,70),(39,39,3,0,50,50);
/*!40000 ALTER TABLE `booking_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `booking_date` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (1,1,'2025-01-10 00:00:00.000000','COMPLETED','2025-11-25 15:56:26'),(2,2,'2025-01-12 00:00:00.000000','PENDING','2025-11-25 15:56:26'),(3,3,'2025-01-15 00:00:00.000000','CONFIRMED','2025-11-25 15:56:26'),(4,1,'2025-11-14 00:00:00.000000','CANCELLED','2025-11-27 07:54:13'),(5,1,'2026-01-25 00:48:13.994331','Pending','2026-01-24 16:48:14'),(6,1,'2026-01-26 12:40:01.573528','Cancelled','2026-01-26 04:40:01'),(7,1,'2026-01-26 13:04:46.102097','Cancelled','2026-01-26 05:04:46'),(8,1,'2026-01-26 13:04:59.358968','Pending','2026-01-26 05:04:59'),(9,1,'2026-01-26 13:19:44.504278','Pending','2026-01-26 05:19:44'),(10,1,'2026-01-26 13:24:29.109783','Paid','2026-01-26 05:24:29'),(11,1,'2026-01-26 13:24:29.226589','Paid','2026-01-26 05:24:29'),(12,1,'2026-01-26 13:25:00.940185','Pending','2026-01-26 05:25:00'),(13,1,'2026-01-26 13:25:00.962960','Pending','2026-01-26 05:25:00'),(14,1,'2026-01-26 13:38:19.691516','Pending','2026-01-26 05:38:19'),(15,1,'2026-01-26 13:38:25.902971','Pending','2026-01-26 05:38:25'),(16,1,'2026-01-26 17:26:38.311387','Pending','2026-01-26 09:26:38'),(17,1,'2026-01-26 17:34:48.130288','Cancelled','2026-01-26 09:34:48'),(18,1,'2026-01-26 17:36:35.904403','Pending','2026-01-26 09:36:35'),(19,1,'2026-01-26 17:36:53.109843','Pending','2026-01-26 09:36:53'),(22,1,'2026-01-26 17:42:28.454806','Pending','2026-01-26 09:42:28'),(23,1,'2026-01-26 17:47:39.537563','Pending','2026-01-26 09:47:39'),(27,1,'2026-01-26 17:53:38.178005','Pending','2026-01-26 09:53:38'),(28,1,'2026-01-26 18:13:35.524400','PENDING','2026-01-26 10:13:35'),(36,1,'2026-01-26 18:15:45.077187','PENDING','2026-01-26 10:15:45'),(37,4,'2026-01-26 18:24:21.941193','PENDING','2026-01-26 10:24:21'),(39,4,'2026-01-26 18:25:12.427769','PENDING','2026-01-26 10:25:12');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `caregivers`
--

DROP TABLE IF EXISTS `caregivers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `caregivers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `qualifications` text,
  `image_path` varchar(255) DEFAULT NULL,
  `availability_status` varchar(30) DEFAULT 'Available',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `caregivers`
--

LOCK TABLES `caregivers` WRITE;
/*!40000 ALTER TABLE `caregivers` DISABLE KEYS */;
/*!40000 ALTER TABLE `caregivers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_carts`
--

DROP TABLE IF EXISTS `customer_carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_carts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cart_data` tinytext,
  `customer_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK77sv72eswoj70w0xrrvyehrik` (`customer_id`),
  CONSTRAINT `FK394vfatloh97hrbkjiatsear5` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_carts`
--

LOCK TABLES `customer_carts` WRITE;
/*!40000 ALTER TABLE `customer_carts` DISABLE KEYS */;
INSERT INTO `customer_carts` VALUES (3,'4',4);
/*!40000 ALTER TABLE `customer_carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'Alice Tan','alice@example.com','alice123','91234561','123 Clementi Ave','2025-11-25 15:56:26'),(2,'John Lim','john@example.com','john123','98765432','456 Jurong West','2025-11-25 15:56:26'),(3,'Mary Lee','mary@example.com','mary123','90011223','789 Bukit Batok','2025-11-25 15:56:26'),(4,'123','123@gmail.com','123','123','123','2026-01-26 10:24:02');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feedback`
--

DROP TABLE IF EXISTS `feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feedback` (
  `id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int NOT NULL,
  `service_id` int NOT NULL,
  `rating` int DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `customer_id` (`customer_id`),
  KEY `service_id` (`service_id`),
  CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `feedback_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`),
  CONSTRAINT `feedback_chk_1` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feedback`
--

LOCK TABLES `feedback` WRITE;
/*!40000 ALTER TABLE `feedback` DISABLE KEYS */;
INSERT INTO `feedback` VALUES (1,1,1,5,'Excellent caregiver, very patient!','2025-11-25 15:56:26'),(2,2,5,4,'Helpful but could be more punctual.','2025-11-25 15:56:26'),(3,3,7,5,'Very good wheelchair assistance.','2025-11-25 15:56:26'),(4,1,5,4,'Really helpful for my grandmother who has dementia','2025-11-26 15:12:38'),(5,1,10,5,'Good','2025-12-03 03:21:39');
/*!40000 ALTER TABLE `feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int NOT NULL,
  `amount_excl_gst` decimal(10,2) NOT NULL,
  `gst_amount` decimal(10,2) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `payment_status` enum('Pending','Paid','Failed','Refunded') DEFAULT 'Pending',
  `payment_method` enum('Credit Card','PayPal','PayNow','Cash') DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `amount` double NOT NULL,
  `method` varchar(255) DEFAULT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `transaction_ref` varchar(255) DEFAULT NULL,
  `customer_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `booking_id` (`booking_id`),
  KEY `FK45dp0030s8e3myd8n6ky4e79g` (`customer_id`),
  CONSTRAINT `FK45dp0030s8e3myd8n6ky4e79g` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_calculate_gst` BEFORE INSERT ON `payments` FOR EACH ROW BEGIN
    SET NEW.gst_amount = ROUND(NEW.amount_excl_gst * 0.09, 2);
    SET NEW.total_amount = ROUND(NEW.amount_excl_gst + NEW.gst_amount, 2);
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `service`
--

DROP TABLE IF EXISTS `service`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `service_name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `image_path` varchar(255) DEFAULT 'images/default.png',
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `service_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `service_category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service`
--

LOCK TABLES `service` WRITE;
/*!40000 ALTER TABLE `service` DISABLE KEYS */;
INSERT INTO `service` VALUES (1,1,'Home Checkup','Caregiver visits your home daily.',80,'images/service1.png'),(2,1,'Side Compainionship Service','Provide social and emotional support.',80,'images/service2.png'),(3,1,'Fresh Meals Preparation','Prepare healthy meals for seniors and elderly.',50,'images/service3.png'),(4,2,'Nursing & Care','Qualified nurse for medical needs.',120,'images/service4.png'),(5,2,'Medication Reminder','Ensure seniors take meds on time.',40,'images/service5.png'),(6,2,'Vital Sign Monitoring','Daily monitoring of vital health signs.',70,'images/service6.png'),(7,3,'Wheelchair Assistance','Help with mobility & wheelchair use.',45,'images/service7.png'),(8,3,'Home Cleaning Support','General cleaning for elderly homes.',55,'images/service8.png'),(9,3,'Transport to Appointments','Escort to clinics/hospitals.',90,'images/service9.png'),(10,1,'Fresh Meals Preparation1','1123',52,'');
/*!40000 ALTER TABLE `service` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_category`
--

DROP TABLE IF EXISTS `service_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_category`
--

LOCK TABLES `service_category` WRITE;
/*!40000 ALTER TABLE `service_category` DISABLE KEYS */;
INSERT INTO `service_category` VALUES (1,'Elderly Home Care','Daily elderly assistance and care'),(2,'Medical Assistance','Nursing, medication and health monitoring'),(3,'Mobility & Household Support','Transport and home cleaning support');
/*!40000 ALTER TABLE `service_category` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-26 18:41:40
