const fs = require('fs');

const files = [
    'D:/TaoYuan2023_HSTG/HSTG/MODCreate/fseticket/src/main/java/net/fsefmgftc/fseticket/client/audio/FSEAudioStream.java',
    'D:/TaoYuan2023_HSTG/HSTG/MODCreate/fseticket/src/main/java/net/fsefmgftc/fseticket/cc/BroadcastHostPeripheral.java',
    'D:/TaoYuan2023_HSTG/HSTG/MODCreate/fseticket/src/main/java/net/fsefmgftc/fseticket/network/SpeakerAudioPacket.java',
    'D:/TaoYuan2023_HSTG/HSTG/MODCreate/fseticket/src/main/java/net/fsefmgftc/fseticket/client/audio/SpeakerClientHandler.java'
];

files.forEach(f => {
    if (!fs.existsSync(f)) return;
    let content = fs.readFileSync(f, 'utf8');
    if (content.charCodeAt(0) === 0xFEFF) {
        content = content.slice(1);
        fs.writeFileSync(f, content, 'utf8');
        console.log('Fixed BOM in ' + f);
    }
});