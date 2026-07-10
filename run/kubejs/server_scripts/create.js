ServerEvents.recipes(event =>{
    const create = event.recipes.create

    create.mixing('2x kubejs:brust_alloy',['minecraft:diamond','kubejs:high_performance_explosive','minecraft:netherite_ingot']).superheated()
    create.mixing('2x kubejs:brust_alloy',['minecraft:diamond','8x minecraft:tnt','minecraft:netherite_ingot']).superheated()
    create.mixing('2x kubejs:brust_alloy',['minecraft:diamond','2x kubejs:high_performance_explosive','minecraft:netherite_ingot']).heated()
    create.mixing('2x kubejs:brust_alloy',['minecraft:diamond','16x minecraft:tnt','minecraft:netherite_ingot']).heated()
    create.mixing('4x kubejs:high_performance_explosive',['kubejs:glass_crest','kubejs:night_crest','minecraft:tnt']).superheated()
    create.mixing(["kubejs:mithril_ingot","2x kubejs:mithril_nugget"],'kubejs:raw_mithril').superheated()
    create.mixing(["kubejs:adamantium_ingot","2x kubejs:adamantium_nugget"],'kubejs:raw_adamantium').superheated()
    create.mixing(["kubejs:mithril_ingot"],'kubejs:raw_mithril').heated()
    create.mixing(["kubejs:adamantium_ingot"],'kubejs:raw_adamantium').heated()
    create.mixing('kubejs:adamantium_ore',["kubejs:raw_adamantium","kubejs:compressed_basalt"]).heated()
    create.mixing('kubejs:mithril_ore',["kubejs:raw_mithril","kubejs:compressed_basalt"]).heated()
    create.mixing(Fluid.of('kubejs:molten_immortal'),[Fluid.of('minecraft:lava'),"kubejs:adamantium_dust","kubejs:mithril_dust"]).heated()
    create.mixing('kubejs:raw_immortal',['kubejs:molten_immortal',"16x kubejs:crushed_basalt"]).superheated()
    create.mixing(
        [CreateItem.of("kubejs:catalytic_substrate_blank",0.25),'kubejs:immortal_alloy_nugget'],
        ['kubejs:raw_immortal',"kubejs:catalytic_substrate_blank","kubejs:adamantium_ingot"]
    ).superheated()
    create.mixing(
        [CreateItem.of("kubejs:diamond_catalytic_substrate",0.5),'2x kubejs:immortal_alloy_nugget'],
        ['kubejs:raw_immortal',"kubejs:diamond_catalytic_substrate","kubejs:adamantium_ingot"]
    ).superheated()
    create.mixing(
        [CreateItem.of("kubejs:emerald_catalytic_substrate",0.75),'4x kubejs:immortal_alloy_nugget'],
        ['kubejs:raw_immortal',"kubejs:emerald_catalytic_substrate","kubejs:adamantium_ingot"]
    ).superheated()
    create.mixing(
        ["kubejs:netherite_catalytic_substrate",'6x kubejs:immortal_alloy_nugget'],
        ['kubejs:raw_immortal',"kubejs:netherite_catalytic_substrate","kubejs:adamantium_ingot"]
    ).superheated()
    create.mixing(
        [CreateItem.of("kubejs:crest_catalytic_substrate"),'kubejs:immortal_alloy_ingot'],
        ['kubejs:raw_immortal',"kubejs:crest_catalytic_substrate","kubejs:adamantium_ingot"]
    ).superheated()
    create.mixing(
        [CreateItem.of("kubejs:catalytic_substrate_blank",0.25),'kubejs:immortal_alloy_nugget'],
        ['kubejs:raw_immortal',"kubejs:catalytic_substrate_blank","kubejs:mithril_ingot"]
    ).superheated()
    create.mixing(
        [CreateItem.of("kubejs:diamond_catalytic_substrate",0.5),'2x kubejs:immortal_alloy_nugget'],
        ['kubejs:raw_immortal',"kubejs:diamond_catalytic_substrate","kubejs:mithril_ingot"]
    ).superheated()
    create.mixing(
        [CreateItem.of("kubejs:emerald_catalytic_substrate",0.75),'4x kubejs:immortal_alloy_nugget'],
        ['kubejs:raw_immortal',"kubejs:emerald_catalytic_substrate","kubejs:mithril_ingot"]
    ).superheated()
    create.mixing(
        [CreateItem.of("kubejs:netherite_catalytic_substrate"),'6x kubejs:immortal_alloy_nugget'],
        ['kubejs:raw_immortal',"kubejs:netherite_catalytic_substrate","kubejs:mithril_ingot"]
    ).superheated()
    create.mixing(
        [CreateItem.of("kubejs:crest_catalytic_substrate"),'kubejs:immortal_alloy_ingot'],
        ['kubejs:raw_immortal',"kubejs:netherite_catalytic_substrate","kubejs:mithril_ingot"]
    ).superheated()


    create.compacting("kubejs:compressed_basalt","64x basalt")
    create.compacting("3x kubejs:raw_adamantium",'kubejs:adamantium_block')
    create.compacting("3x kubejs:raw_mithril",'kubejs:mithril_block')
    create.compacting(["kubejs:raw_adamantium","kubejs:adamantium_nugget"],'kubejs:adamantium_ore')
    create.compacting(["kubejs:raw_mithril","kubejs:mithril_nugget"],'kubejs:mithril_ore')

    create.crushing([
        CreateItem.of('kubejs:adamantium_nugget',0.2),
        CreateItem.of('kubejs:mithril_nugget',0.2),
        CreateItem.of('netherite_scrap',0.4),
        CreateItem.of('diamond',0.6),
        '4x kubejs:crushed_basalt'
    ],
    'kubejs:compressed_basalt').processingTime(10 * 200)

    create.milling('kubejs:adamantium_dust', 'kubejs:adamantium_ingot')
    create.milling('kubejs:mithril_dust', 'kubejs:mithril_ingot')
})