ServerEvents.tags('item', event =>{
    event.add('forge:ingots/brust_alloy','kubejs:brust_alloy')
    event.add('forge:ingots/adamantium_ingot','kubejs:adamantium_ingot')
    event.add('forge:ingots/mithril_ingot','kubejs:mithril_ingot')
    event.add('forge:ingots/immortal_ingot','kubejs:immortal_alloy_ingot')
})
ServerEvents.tags('block', event =>{
event.add('minecraft:beacon_base_blocks',
    ['kubejs:brust_alloy_block','kubejs:adamantium_block','kubejs:mithril_block','kubejs:immortal_alloy_block','kubejs:compressed_diamond_block','kubejs:compressed_emerald_block','kubejs:compressed_netherite_block'
    ])
})
ServerEvents.tags('tools', event =>{
    event.add('fse_expanded:burst_alloy_tool',['kubejs:burst_alloy_pickaxe','kubejs:burst_alloy_hoe','kubejs:burst_alloy_axe','kubejs:burst_alloy_shovel','kubejs:burst_alloy_sword'])
    event.add('fse_expanded:adamantium_tool',['kubejs:adamantium_pickaxe','kubejs:adamantium_hoe','kubejs:adamantium_axe','kubejs:adamantium_shovel','kubejs:adamantium_sword'])
    event.add('fse_expanded:mithril_tool',['kubejs:mithril_pickaxe','kubejs:mithril_hoe','kubejs:mithril_axe','kubejs:mithril_shovel','kubejs:mithril_sword'])
    event.add('fse_expanded:immortal_alloy_tool',['kubejs:immortal_alloy_pickaxe','kubejs:immortal_alloy_hoe','kubejs:immortal_alloy_axe','kubejs:immortal_alloy_shovel','kubejs:immortal_alloy_sword'])
})