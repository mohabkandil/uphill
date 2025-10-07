const express = require('express');
const app = express();
app.use(express.json());

// Log all requests
app.use((req, res, next) => {
  console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
  console.log('Headers:', req.headers);
  console.log('Body:', JSON.stringify(req.body, null, 2));
  next();
});

const ok = (req, res) => {
  console.log(`Responding to ${req.path} with 200 OK`);
  res.status(200).json({ status: 'ok' });
};

app.post('/doctor-calendar', ok);
app.post('/room-reservation', ok);
app.post('/email-notification', ok);

const port = process.env.PORT || 3001;
app.listen(port, () => {
  console.log(`Mock external server listening on ${port}`);
});


