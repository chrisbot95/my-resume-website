const express = require('express');
const { exec } = require('child_process');
const session = require('express-session');
const fs = require('fs');
const path = require('path');
const cron = require('node-cron');

const app = express();

// Functions
function copyDirectory(source, destination) {
  // Ensure the provided paths are absolute
  const sourcePath = path.resolve(source);
  const destinationPath = path.resolve(destination);

  // Create destination directory if it doesn't exist
  if (!fs.existsSync(destinationPath)) {
    fs.mkdirSync(destinationPath, { recursive: true });
  }

  // Read the content of the source directory
  const files = fs.readdirSync(sourcePath);

  // Iterate through each file and copy it to the destination
  files.forEach(file => {
    const sourceFilePath = path.join(sourcePath, file);
    const destinationFilePath = path.join(destinationPath, file);

    // Check if it's a file or a directory
    const isDirectory = fs.statSync(sourceFilePath).isDirectory();

    if (isDirectory) {
      // Recursively copy subdirectories
      copyDirectory(sourceFilePath, destinationFilePath);
    } else {
      // Copy the file
      fs.copyFileSync(sourceFilePath, destinationFilePath);
    }
  });
}

function clearDirectory(directoryPath) {
  // Ensure the provided path is absolute
  const absolutePath = path.resolve(directoryPath);

  // Read the content of the directory
  const files = fs.readdirSync(absolutePath);

  // Iterate through each file and remove it (including subdirectories)
  files.forEach(file => {
    const filePath = path.join(absolutePath, file);

    // Check if it's a file or a directory
    const isDirectory = fs.statSync(filePath).isDirectory();

    if (isDirectory) {
      // Recursively clear subdirectories
      clearDirectory(filePath);
      // Remove the empty directory
      fs.rmdirSync(filePath);
    } else {
      // Remove the file
      fs.unlinkSync(filePath);
    }
  });
}


app.use(
  session({
    secret: 'your-secret-key',
    resave: false,
    saveUninitialized: true,
    cookie: {
      maxAge: 60 * 60 * 1000 // Set the session cookie to expire after 1 hour (in milliseconds)
    }
  })
);

//app.use(express.static(path.join(__dirname, '../public')));

cron.schedule('0 1 * * *', () => {
  clearDirectory(path.join(__dirname, 'users'));
});


app.get('/', (req, res) => {
  if (!req.session.user) {
    req.session.user = 'user_' + Date.now();
  }
  const userFolder = path.join(__dirname, 'users', req.session.user);
  //const todoFolder = path.join(__dirname, 'users', req.session.user, 'todo');
  const templatesFolder = path.join(__dirname, 'templates', 'todo');
  //const assistantFolder = path.join(__dirname, 'users', req.session.user, 'assistant');
  if (!fs.existsSync(userFolder)) {
    fs.mkdirSync(userFolder);
    //fs.mkdirSync(todoFolder);
    //fs.mkdirSync(assistantFolder);

    // Copy template.txt to user folder as todo.txt
    copyDirectory(templatesFolder, userFolder);
  }

  // Send the index.html file as the response
  res.sendFile(path.join(__dirname, '..', 'public', 'index.html'));
});

app.get('/todo', (req, res) => {
  // Send the todo.html file as the response
  res.sendFile(path.join(__dirname, '..', 'public', 'todo.html'));
});

app.get('/execute', (req, res) => {
  const command = req.query.command;
  //const javaFolderPath = path.join(__dirname, 'todo');
  exec(`java -cp . Todo ${req.session.user} ${command}`, (error, stdout, stderr) => {
      if (error) {
          console.error(`Error executing Java file: ${error.message}`);
          res.status(500).send('Internal Server Error');
          return;
      }
      res.send(stdout);
  });
});

app.get('/logout', (req, res) => {
  // Manual logout route
  // You should implement a session expiration mechanism in a production environment
  const userFolder = path.join(__dirname, 'users', req.session.user);

  // Delete user folder and its contents
  fs.rmdirSync(userFolder, { recursive: true });

  // Destroy the session
  req.session.destroy((err) => {
    if (err) {
      console.error('Error destroying session:', err);
    } else {
      res.send('Logged out successfully! User folder deleted.');
    }
  });
});

const PORT = 80;
app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
