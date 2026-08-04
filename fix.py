# did you forget to ignore this file

import os
import re

audio_dir = 'src/main/java/net/fsefmgftc/fseticket/client/audio'

def replace_in_file(path, replacements):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    for old, new in replacements:
        content = content.replace(old, new)
    # Also do regex replacements if any
    content = re.sub(r'com\.tom\.hqspeaker\.network\.HQSpeakerNetwork\.sendToServer\(.*?\);', '', content, flags=re.DOTALL)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

replace_in_file(os.path.join(audio_dir, 'SpeakerClientHandler.java'), [
    ('HQSpeakerAudioPacket', 'SpeakerAudioPacket'),
    ('HQSpeakerClientHandler', 'SpeakerClientHandler'),
    ('net.minecraftforge.api.distmarker', 'net.neoforged.api.distmarker'),
    ('new ResourceLocation("hqspeaker", "hq_speaker")', 'ResourceLocation.fromNamespaceAndPath("fseticket", "hq_speaker")')
])

replace_in_file(os.path.join(audio_dir, 'HQAudioStream.java'), [
    ('HQSpeakerAudioPacket', 'SpeakerAudioPacket'),
])

replace_in_file(os.path.join(audio_dir, 'SharedStreamingGroup.java'), [
    ('HQSpeakerAudioPacket', 'SpeakerAudioPacket'),
])

packet_dir = 'src/main/java/net/fsefmgftc/fseticket/network'
for file in ['SpeakerAudioPacket.java', 'SpeakerStopPacket.java']:
    with open(os.path.join(packet_dir, file), 'r', encoding='utf-8') as f:
        content = f.read()
    content = content.replace('StreamCodec.of(', 'StreamCodec.ofMember(')
    with open(os.path.join(packet_dir, file), 'w', encoding='utf-8') as f:
        f.write(content)

print("Fixed!")
