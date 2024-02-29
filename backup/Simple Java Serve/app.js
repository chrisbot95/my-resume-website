const express = require('express');
const { exec } = require('child_process');
const path = require('path'); // Import the 'path' module
const app = express();
const port = 3000;

// Serve static files from the 'public' directory
app.use(express.static(path.join(__dirname, '../public')));

app.get('/execute', (req, res) => {
    const command = req.query.command;
    exec(`java -cp . Todo ${command}`, (error, stdout, stderr) => {
        if (error) {
            console.error(`Error executing Java file: ${error.message}`);
            res.status(500).send('Internal Server Error');
            return;
        }
        res.send(stdout);
    });
});

app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
