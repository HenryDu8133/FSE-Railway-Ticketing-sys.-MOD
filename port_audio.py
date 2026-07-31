import os
import re
import shutil

src_dir = 'C:/Users/19764/AppData/Local/Temp/CCHQSpeakers/src/main/java/com/tom/hqspeaker/client'
dst_dir = 'src/main/java/net/fsefmgftc/fseticket/client/audio'

files_to_port = [
    ('audio/HLSPlaylistParser.java', 'HLSPlaylistParser.java'),
    ('audio/SharedStreamingGroup.java', 'SharedStreamingGroup.java'),
    ('audio/StreamingAudioSource.java', 'StreamingAudioSource.java'),
    ('audio/TSDemuxer.java', 'TSDemuxer.java'),
    ('HQAudioStream.java', 'HQAudioStream.java'),
    ('HQSpeakerClientHandler.java', 'SpeakerClientHandler.java')
]

os.makedirs(dst_dir, exist_ok=True)

for src_file, dst_file in files_to_port:
    with open(os.path.join(src_dir, src_file), 'r', encoding='utf-8') as f:
        content = f.read()

    # Refactor packages
    content = content.replace('package com.tom.hqspeaker.client.audio;', 'package net.fsefmgftc.fseticket.client.audio;')
    content = content.replace('package com.tom.hqspeaker.client;', 'package net.fsefmgftc.fseticket.client.audio;')
    
    # Refactor imports
    content = content.replace('com.tom.hqspeaker.client.audio.', 'net.fsefmgftc.fseticket.client.audio.')
    content = content.replace('com.tom.hqspeaker.client.HQAudioStream', 'net.fsefmgftc.fseticket.client.audio.HQAudioStream')
    content = content.replace('com.tom.hqspeaker.network.HQSpeakerAudioPacket', 'net.fsefmgftc.fseticket.network.SpeakerAudioPacket')
    content = content.replace('com.tom.hqspeaker.HQSpeakerMod', 'net.fsefmgftc.fseticket.FseticketMod')
    content = content.replace('HQSpeakerMod.log', 'System.out.println')
    content = content.replace('HQSpeakerMod.warn', 'System.err.println')
    content = content.replace('HQSpeakerMod.error', 'System.err.println')

    # Remove VS2
    content = re.sub(r'import com\.tom\.hqspeaker\.vs2\..*?;\n', '', content)
    content = content.replace('if (!VS2TransformHelper.isVS2Loaded()) return;', '')
    content = re.sub(r'Object ship = VS2TransformHelper\.getShipManagingBlock\(.*?\);.*?if \(ship == null\) return;', '', content, flags=re.DOTALL)
    content = re.sub(r'Matrix4dc mat = getShipToWorldMatrix\(ship\);.*?if \(mat == null\) return;', '', content, flags=re.DOTALL)
    content = re.sub(r'static Matrix4dc getShipToWorldMatrix.*?\}', '', content, flags=re.DOTALL)

    # In SpeakerState.tickPosition
    content = re.sub(r'void tickPosition\(Level level\).*?sound\.update\(\(float\) w\.x, \(float\) w\.y, \(float\) w\.z\);.*?\} catch.*?\}', r'''void tickPosition(Level level) {
            if (sound == null || lastPkt == null || level == null) return;
            Minecraft mc = Minecraft.getInstance();
            if (!mc.getSoundManager().isActive(sound)) return;
            sound.update((float) lastPkt.x, (float) lastPkt.y, (float) lastPkt.z);
        }''', content, flags=re.DOTALL)

    # Remove org.joml imports
    content = re.sub(r'import org\.joml\..*?;\n', '', content)

    with open(os.path.join(dst_dir, dst_file), 'w', encoding='utf-8') as f:
        f.write(content)

print("Porting complete!")
