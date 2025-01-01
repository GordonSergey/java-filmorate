-- schema.sql
CREATE TABLE IF NOT EXISTS User (
    UserID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    Login VARCHAR(255) NOT NULL,
    Birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS Film (
    FilmID INT PRIMARY KEY AUTO_INCREMENT,
    Title VARCHAR(255) NOT NULL,
    Description TEXT,
    ReleaseDate DATE NOT NULL,
    Duration INT NOT NULL,
    MPAID INT NOT NULL,
    GenreID INT,
    FOREIGN KEY (MPAID) REFERENCES MpaRating (MPARatingID),
    FOREIGN KEY (GenreID) REFERENCES Genre (GenreID)
);

CREATE TABLE IF NOT EXISTS Genre (
    GenreID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS MpaRating (
    MPARatingID INT PRIMARY KEY AUTO_INCREMENT,
    Rating VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS Friendship (
    UserID_1 INT NOT NULL,
    UserID_2 INT NOT NULL,
    Status VARCHAR(50),
    PRIMARY KEY (UserID_1, UserID_2),
    FOREIGN KEY (UserID_1) REFERENCES User(UserID),
    FOREIGN KEY (UserID_2) REFERENCES User(UserID)
);

CREATE TABLE IF NOT EXISTS Like (
    UserID INT NOT NULL,
    FilmID INT NOT NULL,
    LikeStatus BOOLEAN NOT NULL,
    PRIMARY KEY (UserID, FilmID),
    FOREIGN KEY (UserID) REFERENCES User(UserID),
    FOREIGN KEY (FilmID) REFERENCES Film(FilmID)
);