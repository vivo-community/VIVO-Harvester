-- phpMyAdmin SQL Dump
-- version 3.3.9.2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 22, 2011 at 12:27 PM
-- Server version: 5.5.9
-- PHP Version: 5.3.5

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `DemoDB`
--

-- --------------------------------------------------------

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
CREATE TABLE IF NOT EXISTS `department` (
  `dept_id` varchar(8) NOT NULL,
  `dept_name` varchar(255) NOT NULL,
  `type_id` int(3) NOT NULL,
  `super_dept_id` varchar(8) NOT NULL,
  PRIMARY KEY (`dept_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `department`
--

INSERT INTO `department` (`dept_id`, `dept_name`, `type_id`, `super_dept_id`) VALUES
('12345678', 'Division of Medicine', 402, '00000000'),
('87654321', 'Department of Research Informatics', 403, '12345678'),
('09876543', 'Clinical and Translational Program', 405, '87654321'),
('00000000', 'University of Sample Data', 401, '');

-- --------------------------------------------------------

--
-- Table structure for table `job`
--

DROP TABLE IF EXISTS `job`;
CREATE TABLE IF NOT EXISTS `job` (
  `dept_id` varchar(8) NOT NULL,
  `person_id` varchar(10) NOT NULL,
  `type_id` int(3) NOT NULL,
  `start_date` date NOT NULL,
  PRIMARY KEY (`dept_id`,`person_id`,`type_id`,`start_date`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `job`
--

INSERT INTO `job` (`dept_id`, `person_id`, `type_id`, `start_date`) VALUES
('00000000', '78A54C5AGB', 121, '2010-03-11'),
('00000000', 'HDJFCH54YA', 122, '2009-07-01'),
('09876543', '78A5G4A7A8', 392, '2006-05-29'),
('09876543', '8AG46A4G6A', 393, '2000-02-06'),
('09876543', 'NHA6456A4M', 393, '1995-03-21'),
('12345678', '541AFA1CA1', 254, '2007-07-13'),
('12345678', '78J68SD654', 254, '2001-04-22'),
('12345678', '7GA978A648', 254, '2004-06-10'),
('12345678', 'KJ7S435S8B', 254, '2007-09-15'),
('87654321', '4A84HABAS4', 391, '2010-10-13'),
('87654321', 'KJ8S1SJ9S5', 391, '2009-12-04');

-- --------------------------------------------------------

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
CREATE TABLE IF NOT EXISTS `person` (
  `person_id` varchar(10) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `middle_name` varchar(255) NOT NULL,
  `prefix_name` varchar(255) NOT NULL,
  `suffix_name` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `official_name` varchar(255) NOT NULL,
  `pref_title` varchar(255) NOT NULL,
  `email_address` varchar(255) NOT NULL,
  `work_phone` bigint(255) NOT NULL,
  `work_fax` bigint(255) NOT NULL,
  `publish_ok` int(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`person_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `person`
--

INSERT INTO `person` (`person_id`, `first_name`, `last_name`, `middle_name`, `prefix_name`, `suffix_name`, `full_name`, `official_name`, `pref_title`, `email_address`, `work_phone`, `work_fax`, `publish_ok`) VALUES
('541AFA1CA1', 'Vince', 'Protle', 'Charles', 'Dr.', '', 'Dr. Vince Protle', 'Vince Charles Protle', 'Research Professor', 'vince.protle@sample.edu', 1239837282, 1239832437, 1),
('4A84HABAS4', 'Calvin', 'Tolen', 'A.', '', 'Sr.', 'Calvin Tolen', 'Calvin A. Tolen Sr.', 'Software Developer', 'calvin.tolen@sample.edu', 1239837373, 1239835492, 1),
('78A54C5AGB', 'Rebecca', 'Keller', 'B.', 'Mrs.', '', 'Rebecca Keller', 'Rebecca B. Keller', 'President', 'rebecca.keller@sample.edu', 1239838638, 1239831387, 1),
('7GA978A648', 'Todd', 'Miller', '', 'Dr.', '', 'Dr. Todd Miller', 'Todd Miller', 'Research Professor', 'todd.miller@sample.edu', 1239838621, 1239832163, 1),
('KJ8S1SJ9S5', 'Zheng', 'Bao', 'Li', 'Mr.', '', 'Zheng Li Bao', 'Zheng Li Bao', 'Software Developer', 'zheng.bao@sample.edu', 1239837221, 1239839864, 0),
('78A5G4A7A8', 'Warren', 'Samson', '', 'Dr.', '', 'Dr. Warren Samson', 'Warren Samson', 'Software Engineer', 'warren.samson@sample.edu', 1239835431, 1239839713, 1),
('NHA6456A4M', 'Richard', 'Anders', 'G.', '', 'Sr.', 'Dick Anders', 'Richard G. Anders Sr.', 'Senior Software Engineer', 'richard.anders@sample.edu', 1239837951, 1239838437, 1),
('78J68SD654', 'Malish', 'Nakael', '', '', '', 'Malish Nakael', 'Malish Nakael', 'Research Professor', 'malish.nakael@sample.edu', 1239837651, 1239838493, 1),
('8AG46A4G6A', 'Naomi', 'Faen', 'V.', '', '', 'Naomi Faen', 'Naomi V. Faen', 'Senior Software Engineer', 'naomi.faen@sample.edu', 1239837984, 1239837961, 1),
('KJ7S435S8B', 'Jürg', 'Linsten', 'A.', '', '', 'Jürg Linsten', 'Jürg A. Linsten', 'Research Professor', 'jurg.linsten@sample.edu', 1239837647, 1239839499, 1),
('HDJFCH54YA', 'Jeran', 'Tirea', 'C', '', '', 'Jeran Tirea', 'Jeran C. Tirea', 'Chief Information Officer', 'jeran.tirea@sample.edu', 1239838984, 1239834841, 1);

-- --------------------------------------------------------

--
-- Table structure for table `type`
--

DROP TABLE IF EXISTS `type`;
CREATE TABLE IF NOT EXISTS `type` (
  `type_id` int(5) NOT NULL AUTO_INCREMENT,
  `value` varchar(255) NOT NULL,
  `super_type_id` int(5) NOT NULL,
  PRIMARY KEY (`type_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=407 ;

--
-- Dumping data for table `type`
--

INSERT INTO `type` (`type_id`, `value`, `super_type_id`) VALUES
(120, 'Administration', 0),
(121, 'President', 120),
(122, 'Chief Information Officer', 120),
(250, 'Faculty', 0),
(254, 'Researcher', 250),
(390, 'Staff', 0),
(391, 'Programmer', 390),
(392, 'Software Engineer', 390),
(393, 'Senior Software Engineer', 390),
(400, 'Organization', 0),
(401, 'University', 400),
(402, 'Division', 400),
(403, 'Department', 400),
(404, 'College', 400),
(405, 'Program', 400),
(406, 'Institute', 400);

-- --------------------------------------------------------

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `person_id` varchar(10) NOT NULL,
  `login_name` varchar(15) NOT NULL,
  `expired` int(1) NOT NULL,
  PRIMARY KEY (`person_id`),
  UNIQUE KEY `login_name` (`login_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

--
-- Dumping data for table `user`
--

INSERT INTO `user` (`person_id`, `login_name`, `expired`) VALUES
('541AFA1CA1', 'vince.protle', 0),
('4A84HABAS4', 'calvin.tolen', 0),
('78A54C5AGB', 'rebecca.keller', 0),
('7GA978A648', 'todd.miller', 0),
('KJ8S1SJ9S5', 'zheng.bao', 0),
('78A5G4A7A8', 'warren.samson', 0),
('NHA6456A4M', 'richard.anders', 0),
('78J68SD654', 'malish.nakael', 0),
('8AG46A4G6A', 'naomi.faen', 0),
('KJ7S435S8B', 'jurg.linsten', 0),
('HDJFCH54YA', 'jeran.tirea', 0);
