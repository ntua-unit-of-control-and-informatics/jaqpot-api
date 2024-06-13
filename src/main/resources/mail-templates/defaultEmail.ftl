<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Jaqpot Email</title>
    <style>
        .email-body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background-color: #f4f4f4;
        }

        .email-container {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }

        .header {
            background-color: #4F46E5;
            color: #ffffff;
            padding: 20px;
            text-align: center;
        }

        .header img {
            max-width: 250px;
            margin-bottom: 30px;
        }

        .header h1 {
            margin: 0;
            font-size: 24px;
        }

        .content {
            padding: 20px;
            min-height: 200px;
        }

        .content h2 {
            font-size: 20px;
            color: #333333;
        }

        .content p {
            font-size: 16px;
            line-height: 1.6;
            color: #666666;
        }

        .content a {
            font-size: 16px;
        }

        .footer {
            background-color: #f4f4f4;
            color: #666666;
            text-align: center;
            padding: 10px;
            font-size: 14px;
        }
    </style>
</head>
<body>
<div class="email-body">
    <div class="email-container">
        <div class="header">
            <img src="https://i.imgur.com/6wiAkyL.png" alt="Logo">
            <h1>${title}</h1>
        </div>
        <div class="content">
            <h2>Hello, ${recipientName}</h2>
            <p>
                ${bodyContent}
            </p>
            <a href="${actionUrl}">${actionText}</a>
        </div>
        <div class="footer">
            <p>
                &copy; ${.now?string('yyyy')} <a href="https://app.jaqpot.org" target="_blank">Jaqpot</a>. All rights
                reserved.
            </p>
        </div>
    </div>
</div>
</body>
</html>
