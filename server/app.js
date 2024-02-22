const express = require('express');
const path = require('path');

const app = express();
const port = 443; // You can change this to any port you prefer

// Serve static files (including index.html) from the 'public' directory
app.use(express.static(path.join(__dirname, '..', 'public')));

app.listen(port, () => {
  console.log(`Server is running at http://localhost:${port}`);
});
