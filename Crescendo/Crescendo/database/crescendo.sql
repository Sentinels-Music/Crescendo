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
    averageRating DECIMAL(3,2) DEFAULT 0.00
);

CREATE TABLE MusicItems(
    itemId INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    averageRating DECIMAL(3,2) DEFAULT 0.00,
    artistId INT NOT NULL,
    itemType ENUM('ALBUM', 'SONG') NOT NULL,
    FOREIGN KEY (artistId) REFERENCES Artists(artistId)
);

CREATE TABLE Albums(
    itemId INT PRIMARY KEY,
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

-- CHECK
CREATE TABLE ListenLater(
    userID INT NOT NULL,
    itemId INT NOT NULL,
    addedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (userId, itemId),
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE,
    FOREIGN KEY (itemId) REFERENCES MusicItems(itemId) ON DELETE CASCADE
);

CREATE TABLE Tags (
    tagId INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE ArtistTags (
    artistId INT NOT NULL,
    tagId INT NOT NULL,
    PRIMARY KEY (artistId, tagId),
    FOREIGN KEY (artistId) REFERENCES Artists(artistId) ON DELETE CASCADE,
    FOREIGN KEY (tagId) REFERENCES Tags(tagId) ON DELETE CASCADE
);

CREATE TABLE ProfileTags (
    userId INT NOT NULL,
    tagId INT NOT NULL,
    PRIMARY KEY (userId, tagId),
    FOREIGN KEY (userId) REFERENCES Users(userId) ON DELETE CASCADE,
    FOREIGN KEY (tagId) REFERENCES Tags(tagId) ON DELETE CASCADE
);

SELECT 
    Them.userId AS potentialFriendId,
    COUNT(Me.tagId) AS sharedGenresCount
FROM 
    ProfileTags AS Me
JOIN 
    ProfileTags AS Them 
ON 
    Me.tagId = Them.tagId
WHERE 
    Me.userId = 1 AND Them.userId != 1
GROUP BY 
    Them.userId
ORDER BY 
    sharedGenresCount DESC;


SELECT 
    Them.userId AS potentialFriendId,
    ROUND((COUNT(Me.tagId) * 100.0 / (SELECT COUNT(tagId) FROM ProfileTags WHERE userId = 1)), 0) AS tasteMatchPercentage
FROM 
    ProfileTags AS Me
JOIN 
    ProfileTags AS Them 
ON 
    Me.tagId = Them.tagId
WHERE 
    Me.userId = 1 AND Them.userId != 1
GROUP BY 
    Them.userId
ORDER BY 
    tasteMatchPercentage DESC;