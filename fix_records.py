import os
import re

audio_dir = 'src/main/java/net/fsefmgftc/fseticket/client/audio'

fields = ['source', 'format', 'volume', 'x', 'y', 'z', 'blockX', 'blockY', 'blockZ', 'data', 'streamUrl', 'startTick', 'syncGroupId', 'syncGroupSize']

def fix_records(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for field in fields:
        content = re.sub(r'pkt\.' + field + r'\b', 'pkt.' + field + '()', content)
        content = re.sub(r'packet\.' + field + r'\b', 'packet.' + field + '()', content)
        content = re.sub(r'lastPkt\.' + field + r'\b', 'lastPkt.' + field + '()', content)
        content = re.sub(r'message\.' + field + r'\b', 'message.' + field + '()', content)
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

fix_records(os.path.join(audio_dir, 'SpeakerClientHandler.java'))
fix_records(os.path.join(audio_dir, 'HQAudioStream.java'))
fix_records(os.path.join(audio_dir, 'SharedStreamingGroup.java'))
fix_records('src/main/java/net/fsefmgftc/fseticket/network/SpeakerStopPacket.java')

print("Fixed records!")
