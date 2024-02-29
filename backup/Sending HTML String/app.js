const express = require('express');
const { exec } = require('child_process');

const app = express();
const port = 3000;

app.get('/', (req, res) => {
    // Execute Java file using child process
    exec('java -cp . Todo', (error, stdout, stderr) => {
        if (error) {
            console.error(`Error executing Java file: ${error.message}`);
            return res.status(500).send('Internal Server Error');
        }

        // Send the output as a response to the client
        res.send(`
            <!DOCTYPE html>
            <html>
            <head>
                <title>Java Output</title>
            </head>
            <body>
                <h1>Output from Java:</h1>
                ${stdout}
            </body>
            </html>
        `);
    });
});

app.listen(port, () => {
    console.log(`Server is running at http://localhost:${port}`);
});
