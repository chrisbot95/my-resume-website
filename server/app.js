const express = require('express');
const { exec } = require('child_process');
const session = require('express-session');
const fs = require('fs');
const path = require('path');

const app = express();

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

app.get('/', (req, res) => {
  if (!req.session.user) {
    req.session.user = 'user_' + Date.now();
  }
  const userFolder = path.join(__dirname, 'users', req.session.user);
  if (!fs.existsSync(userFolder)) {
    fs.mkdirSync(userFolder);

    // Copy template.txt to user folder as todo.txt
    const templateFilePath = path.join(__dirname, 'template.txt');
    const userTodoFilePath = path.join(userFolder, 'todo.txt');
    fs.copyFileSync(templateFilePath, userTodoFilePath);
  }

  // Send the index.html file as the response
  res.sendFile(path.join(__dirname, '..', 'public', 'index.html'));
});

app.get('/execute', (req, res) => {
  const command = req.query.command;
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
