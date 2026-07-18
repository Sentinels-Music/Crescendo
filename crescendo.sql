CREATE TABLE Users(
    userID INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    passwordHash VARCHAR(250) NOT NULL
);

CREATE TABLE Reviews(
    reviewId INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    itemId INT NOT NULL,
    starRating INT CHECK (starRating >= 1 AND starRating <= 5),
    comment TEXT,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    priorityScore INT CHECK (priorityScore >= 1 AND priorityScore <= 100),
    FOREIGN KEY (userId) REFERENCES Users(userId)
);

CREATE TABLE Artists(
    artistId INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    followerCount INT DEFAULT 0,
    averageRating DECIMAL(3,2) DEFAULT 0.00,
);

CREATE TABLE MusicItem(
    itemId INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    averageRating DECIMAL(3,2) DEFAULT 0.00,
    artistId INT NOT NULL,
    itemType ENUM('ALBUM', 'SONG') NOT NULL,
    FOREIGN KEY (artistId) REFERENCES Artists(artistId)
);

CREATE TABLE Albums(
    itemId INT AUTO_INCREMENT PRIMARY KEY,
    releaseYear INT NOT NULL,
    FOREIGN KEY (itemId) REFERENCES MusicItems(itemId) ON DELETE CASCADE
);

CREATE TABLE Songs (
    itemId INT PRIMARY KEY,
    durationInSeconds INT NOT NULL,
    albumId INT NOT NULL,
    FOREIGN KEY (itemId) REFERENCES MusicItems(itemId) ON DELETE CASCADE,
    FOREIGN KEY (albumId) REFERENCES Albums(itemId)
);
