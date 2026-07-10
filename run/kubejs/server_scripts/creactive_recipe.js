ServerEvents.recipes(event => {
  const Create = event.recipes.create
  //激活创造核心
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:creative_core_active', 0.13), // Main output, will appear in JEI as the result
      CreateItem.of('kubejs:infinite_simulacrum', 0.008), // Rest of these items will be considered Random Salvage
      CreateItem.of('kubejs:mutation_energy_core', 0.008),
      CreateItem.of('kubejs:creative_calculation_chip', 0.09),
      CreateItem.of('kubejs:calculation_matrix_ingot', 0.002),
      CreateItem.of('kubejs:chromatic_shadow_sheet', 0.002),
      CreateItem.of('kubejs:virtual_simulant', 0.002),
      CreateItem.of('kubejs:pressed_calculation_matrix', 0.002)
    ],
    // Input:
    'kubejs:creative_core_inactive', 
    // Sequence:
    [
      Create.cutting('kubejs:creative_core_activing', 'kubejs:creative_core_activing'),
      Create.filling('kubejs:creative_core_activing', ['kubejs:creative_core_activing', Fluid.of('kubejs:rainbow_solution', 1000)]),
      Create.deploying('kubejs:creative_core_activing', ['kubejs:creative_core_activing', 'kubejs:mutation_energy_core',]),
      Create.deploying('kubejs:creative_core_activing', ['kubejs:creative_core_activing', 'kubejs:infinite_simulation_cube',]),
      Create.deploying('kubejs:creative_core_activing', ['kubejs:creative_core_activing', 'kubejs:creative_calculation_chip',]),
      Create.deploying('kubejs:creative_core_activing', ['kubejs:creative_core_activing', 'kubejs:creative_calculation_chip',]),
      Create.deploying('kubejs:creative_core_activing', ['kubejs:creative_core_activing', 'kubejs:virtual_simulant',]),
      Create.deploying('kubejs:creative_core_activing', ['kubejs:creative_core_activing', 'kubejs:virtual_numerical_mass',]),
    ]
  )
  .transitionalItem('kubejs:creative_core_activing')
  .loops(5) // Set the number of loops

//随机合成-计算核心
  Create.sequenced_assembly(
      // Outputs:
      [
        CreateItem.of('kubejs:empty_calculation_core', 0.51), // Main output, will appear in JEI as the result
        CreateItem.of('kubejs:calculation_core_mk1', 0.04),
        CreateItem.of('kubejs:calculation_core_mk2', 0.04),
        CreateItem.of('kubejs:calculation_core_mk3', 0.04),
        CreateItem.of('kubejs:calculation_core_mk4', 0.04),
        CreateItem.of('kubejs:calculation_core_mk5', 0.04),
        CreateItem.of('kubejs:calculation_core_mk6', 0.04),
        CreateItem.of('kubejs:calculation_core_mk7', 0.04),
        CreateItem.of('kubejs:calculation_core_mk8', 0.04),
        CreateItem.of('kubejs:calculation_core_mk9', 0.04),
        CreateItem.of('kubejs:operator_core', 0.1),
        CreateItem.of('kubejs:tree3', 0.03)
      ],
      // Input:
      'ae2:mysterious_cube', 
      // Sequence:
      [
        Create.deploying('kubejs:uncalculationed_core', ['kubejs:uncalculationed_core', 'minecraft:amethyst_shard',]),
        Create.deploying('kubejs:uncalculationed_core', ['kubejs:uncalculationed_core', 'minecraft:amethyst_shard',]),
        Create.deploying('kubejs:uncalculationed_core', ['kubejs:uncalculationed_core', 'create:precision_mechanism',]),
        Create.deploying('kubejs:uncalculationed_core', ['kubejs:uncalculationed_core', 'create:precision_mechanism',]),
        Create.deploying('kubejs:uncalculationed_core', ['kubejs:uncalculationed_core', 'ae2:energy_acceptor',]),
      ]
    )
    .transitionalItem('kubejs:uncalculationed_core')
    .loops(2) // Set the number of loops

    //嬗变宝珠合成
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:mutation_orb', 1), // Main output, will appear in JEI as the result
    ],
    // Input:
    'kubejs:mutation_shard', 
    // Sequence:
    [
      Create.filling('kubejs:mutation_orb', ['kubejs:mutation_orb', Fluid.of('kubejs:mutation_fluid', 1000)])
    ]
  )
  .transitionalItem('kubejs:mutation_orb')
  .loops(10) // Set the number of loops

    //预制异彩化合物合成
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:chromatic_compound_prefab', 1)
    ],
    // Input:
    'kubejs:immortal_alloy_ingot', 
    // Sequence:
    [
      Create.filling('kubejs:immortal_alloy_ingot', ['kubejs:immortal_alloy_ingot', Fluid.of('kubejs:rainbow_solution', 1000)]),
      Create.deploying('kubejs:immortal_alloy_ingot', ['kubejs:immortal_alloy_ingot', 'kubejs:frost_ingot',]),
      Create.deploying('kubejs:immortal_alloy_ingot', ['kubejs:immortal_alloy_ingot', 'kubejs:lumen_ingot',]),
      Create.deploying('kubejs:immortal_alloy_ingot', ['kubejs:immortal_alloy_ingot', 'kubejs:slag_ingot',]),
      Create.pressing('kubejs:immortal_alloy_ingot', 'kubejs:immortal_alloy_ingot')
    ]
  )
  .transitionalItem('kubejs:immortal_alloy_ingot')
  .loops(3) // Set the number of loops
  //精制异彩化合物合成
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:chromatic_compound_normal', 1)
    ],
    // Input:
    'kubejs:chromatic_compound_prefab', 
    // Sequence:
    [
      Create.filling('kubejs:chromatic_compound_prefab', ['kubejs:chromatic_compound_prefab', Fluid.of('kubejs:mutation_fluid', 1000)]),
      Create.deploying('kubejs:chromatic_compound_prefab', ['kubejs:chromatic_compound_prefab', 'kubejs:virtual_numerical_mass',]),
      Create.deploying('kubejs:chromatic_compound_prefab', ['kubejs:chromatic_compound_prefab', 'kubejs:calculation_numerical_mass',]),
      Create.deploying('kubejs:chromatic_compound_prefab', ['kubejs:chromatic_compound_prefab', 'kubejs:virtual_simulant',]),
      Create.filling('kubejs:chromatic_compound_prefab', ['kubejs:chromatic_compound_prefab', Fluid.of('kubejs:mutation_fluid', 1000)])
    ]
  )
  .transitionalItem('kubejs:chromatic_compound_prefab')
  .loops(5) // Set the number of loops
  //异彩化合物制作
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('create:chromatic_compound', 1)
    ],
    // Input:
    'kubejs:chromatic_compound_normal', 
    // Sequence:
    [
      Create.filling('kubejs:chromatic_compound_normal', ['kubejs:chromatic_compound_normal', Fluid.of('kubejs:rainbow_solution')]),
      Create.deploying('kubejs:chromatic_compound_normal', ['kubejs:chromatic_compound_normal', 'kubejs:infinite_simulation_cube',]),
      Create.deploying('kubejs:chromatic_compound_normal', ['kubejs:chromatic_compound_normal', 'kubejs:glowflow_coupler',]),
      Create.deploying('kubejs:chromatic_compound_normal', ['kubejs:chromatic_compound_normal', 'kubejs:infinite_simulation_cube',]),
      Create.filling('kubejs:chromatic_compound_normal', ['kubejs:chromatic_compound_normal', Fluid.of('kubejs:rainbow_solution')])
    ]
  )
  .transitionalItem('kubejs:chromatic_compound_normal')
  .loops(5) // Set the number of loops
  //物品组件
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:item_component', 1)
    ],
    // Input:
    'minecraft:iron_ingot',
    // Sequence:
    [
      Create.deploying('kubejs:item_component', ['kubejs:item_component', 'iron_ingot',]),
      Create.deploying('kubejs:item_component', ['kubejs:item_component', 'iron_block',]),
      Create.deploying('kubejs:item_component', ['kubejs:item_component', 'iron_ingot',]),
    ]
  )
    .transitionalItem('kubejs:item_component')
  .loops(10) // Set the number of loops
  //重型物品组件
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:heavy_item_component', 0.65)
    ],
    // Input:
    'kubejs:item_component',
    // Sequence:
    [
      Create.deploying('kubejs:heavy_item_component', ['kubejs:heavy_item_component', 'diamond',]),
      Create.deploying('kubejs:heavy_item_component', ['kubejs:heavy_item_component', 'gold_block',]),
      Create.deploying('kubejs:heavy_item_component', ['kubejs:heavy_item_component', 'diamond',]),
    ]
  )
    .transitionalItem('kubejs:heavy_item_component')
  .loops(20) // Set the number of loops
  //超重型物品组件
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:super_heavy_item_component', 0.70)
    ],
    // Input:
    'kubejs:heavy_item_component',
    // Sequence:
    [
      Create.deploying('kubejs:super_heavy_item_component', ['kubejs:super_heavy_item_component', 'netherite_ingot',]),
      Create.deploying('kubejs:super_heavy_item_component', ['kubejs:super_heavy_item_component', 'diamond_block',]),
      Create.deploying('kubejs:super_heavy_item_component', ['kubejs:super_heavy_item_component', 'netherite_ingot',]),
    ]
  )
    .transitionalItem('kubejs:super_heavy_item_component')
  .loops(50) // Set the number of loops
  //自适应重型创造物品组件
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:adaptive_heavy_creative_item_component', 0.75)
    ],
    // Input:
    'kubejs:super_heavy_item_component',
    // Sequence:
    [
      Create.filling('kubejs:adaptive_heavy_creative_item_component',['kubejs:adaptive_heavy_creative_item_component',Fluid.of('kubejs:rainbow_solution')]),
      Create.deploying('kubejs:adaptive_heavy_creative_item_component', ['kubejs:adaptive_heavy_creative_item_component', 'kubejs:glowflow_coupler',]),
      Create.deploying('kubejs:adaptive_heavy_creative_item_component', ['kubejs:adaptive_heavy_creative_item_component', 'kubejs:creative_calculation_chip',]),
      Create.deploying('kubejs:adaptive_heavy_creative_item_component', ['kubejs:adaptive_heavy_creative_item_component', 'netherite_block',]),
      Create.deploying('kubejs:adaptive_heavy_creative_item_component', ['kubejs:adaptive_heavy_creative_item_component', 'kubejs:infinite_simulation_cube',]),
      Create.deploying('kubejs:adaptive_heavy_creative_item_component', ['kubejs:adaptive_heavy_creative_item_component', 'netherite_block',]),
      Create.deploying('kubejs:adaptive_heavy_creative_item_component', ['kubejs:adaptive_heavy_creative_item_component', 'kubejs:creative_calculation_chip',]),
      Create.deploying('kubejs:adaptive_heavy_creative_item_component', ['kubejs:adaptive_heavy_creative_item_component', 'kubejs:glowflow_coupler',]),
      Create.filling('kubejs:adaptive_heavy_creative_item_component',['kubejs:adaptive_heavy_creative_item_component',Fluid.of('kubejs:rainbow_solution')])
    ]
  )
    .transitionalItem('kubejs:adaptive_heavy_creative_item_component')
  .loops(40)// Set the number of loops
  //计算矩阵锭
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:calculation_matrix_ingot', 1), // Main output, will appear in JEI as the result
    ],
    // Input:
    'create:chromatic_compound', 
    // Sequence:
    [
      Create.deploying('create:chromatic_compound', ['create:chromatic_compound', 'kubejs:calculation_numerical_mass']),
      Create.deploying('create:chromatic_compound', ['create:chromatic_compound', 'nether_star']),
      Create.deploying('create:chromatic_compound', ['create:chromatic_compound', 'kubejs:calculation_numerical_mass'])
    ]
  )
  .transitionalItem('create:chromatic_compound')
  .loops(20) // Set the number of loops
  //流光耦合器
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:glowflow_coupler', 1)
    ],
    // Input:
    'kubejs:heavy_item_component',
    // Sequence:
    [
      Create.filling('kubejs:glowflow_coupler',['kubejs:glowflow_coupler',Fluid.of('kubejs:rainbow_solution')]),
      Create.deploying('kubejs:glowflow_coupler', ['kubejs:glowflow_coupler', 'netherite_ingot',]),
      Create.deploying('kubejs:glowflow_coupler', ['kubejs:glowflow_coupler', 'kubejs:lumen_ingot',]),
      Create.deploying('kubejs:glowflow_coupler', ['kubejs:glowflow_coupler', 'netherite_ingot',]),
      Create.deploying('kubejs:glowflow_coupler', ['kubejs:glowflow_coupler', 'kubejs:lumen_ingot',]),
      Create.deploying('kubejs:glowflow_coupler', ['kubejs:glowflow_coupler', 'netherite_ingot',]),
      Create.filling('kubejs:glowflow_coupler',['kubejs:glowflow_coupler',Fluid.of('kubejs:glowing_fluid')])
    ]
  )
  .transitionalItem('kubejs:glowflow_coupler')
  .loops(10) // Set the number of loops
  //无限拟合立方
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:infinite_simulation_cube', 1)
    ],
    // Input:
    'kubejs:calculation_matrix_ingot',
    // Sequence:
    [
      Create.filling('kubejs:infinite_simulation_cube',['kubejs:infinite_simulation_cube',Fluid.of('kubejs:rainbow_solution')]),
      Create.deploying('kubejs:infinite_simulation_cube', ['kubejs:infinite_simulation_cube', 'kubejs:calculation_numerical_mass',]),
      Create.deploying('kubejs:infinite_simulation_cube', ['kubejs:infinite_simulation_cube', 'kubejs:calculation_matrix_ingot',]),
      Create.deploying('kubejs:infinite_simulation_cube', ['kubejs:infinite_simulation_cube', 'kubejs:calculation_numerical_mass',]),
      Create.filling('kubejs:infinite_simulation_cube',['kubejs:infinite_simulation_cube',Fluid.of('kubejs:rainbow_solution')])
    ]
  )
  .transitionalItem('kubejs:infinite_simulation_cube')
  .loops(10) // Set the number of loops
  //创造计算芯片
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:creative_calculation_chip', 1)
    ],
    // Input:
    'kubejs:pressed_calculation_matrix',
    // Sequence:
    [
      Create.deploying('kubejs:incomplete_creative_calculation_chip',['kubejs:incomplete_creative_calculation_chip','slime_block']),
      Create.deploying('kubejs:incomplete_creative_calculation_chip', ['kubejs:incomplete_creative_calculation_chip', 'kubejs:calculation_numerical_mass',]),
      Create.deploying('kubejs:incomplete_creative_calculation_chip',['kubejs:incomplete_creative_calculation_chip', 'netherite_ingot']),
      Create.filling('kubejs:incomplete_creative_calculation_chip',['kubejs:incomplete_creative_calculation_chip',Fluid.of('kubejs:rainbow_solution')]),
      Create.deploying('kubejs:incomplete_creative_calculation_chip', ['kubejs:incomplete_creative_calculation_chip', 'kubejs:calculation_numerical_mass',]),
      Create.deploying('kubejs:incomplete_creative_calculation_chip', ['kubejs:incomplete_creative_calculation_chip', 'kubejs:pressed_calculation_matrix',]),
      Create.filling('kubejs:incomplete_creative_calculation_chip',['kubejs:incomplete_creative_calculation_chip',Fluid.of('kubejs:mutation_fluid')])
    ]
  )
  .transitionalItem('kubejs:incomplete_creative_calculation_chip')
  .loops(8) // Set the number of loops
  //嬗变能核
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:mutation_energy_core', 1)
    ],
    // Input:
    'kubejs:mutation_orb',
    // Sequence:
    [
      Create.filling('kubejs:mutation_orb',['kubejs:mutation_orb',Fluid.of('kubejs:rainbow_solution')]),
      Create.deploying('kubejs:mutation_orb', ['kubejs:mutation_orb', 'kubejs:calculation_numerical_mass',]),
      Create.deploying('kubejs:mutation_orb', ['kubejs:mutation_orb', 'kubejs:pressed_calculation_matrix',]),
      Create.deploying('kubejs:mutation_orb', ['kubejs:mutation_orb', 'kubejs:calculation_numerical_mass',]),
      Create.filling('kubejs:mutation_orb',['kubejs:mutation_orb',Fluid.of('kubejs:mutation_fluid')])
    ]
  )
  .transitionalItem('kubejs:mutation_orb')
  .loops(10) // Set the number of loops
  //未激活的创造核心
  Create.sequenced_assembly(
    // Outputs:
    [
      CreateItem.of('kubejs:creative_core_inactive', 1)
    ],
    // Input:
    'kubejs:heavy_item_component',
    // Sequence:
    [
      Create.filling('kubejs:creative_core_inactive',['kubejs:creative_core_inactive',Fluid.of('kubejs:rainbow_solution')]),
      Create.deploying('kubejs:creative_core_inactive', ['kubejs:creative_core_inactive', 'kubejs:chromatic_shadow_sheet',]),
      Create.deploying('kubejs:creative_core_inactive', ['kubejs:creative_core_inactive', 'kubejs:creative_calculation_chip',]),
      Create.filling('kubejs:creative_core_inactive',['kubejs:creative_core_inactive',Fluid.of('kubejs:mutation_fluid')]),
      Create.deploying('kubejs:creative_core_inactive', ['kubejs:creative_core_inactive', 'kubejs:chromatic_shadow_compound_ingot',]),
      Create.filling('kubejs:creative_core_inactive',['kubejs:creative_core_inactive',Fluid.of('kubejs:mutation_fluid')]),
      Create.deploying('kubejs:creative_core_inactive', ['kubejs:creative_core_inactive', 'kubejs:creative_calculation_chip',]),
      Create.deploying('kubejs:creative_core_inactive', ['kubejs:creative_core_inactive', 'kubejs:chromatic_shadow_sheet',]),
      Create.filling('kubejs:creative_core_inactive',['kubejs:creative_core_inactive',Fluid.of('kubejs:rainbow_solution')])
    ]
  )
  .transitionalItem('kubejs:creative_core_inactive')
  .loops(10) // Set the number of loops
  //粉碎-嬗变碎片
Create.crushing([
  CreateItem.of('kubejs:mutation_shard',0.3)
],
'minecraft:emerald').processingTime(10 * 200)
//基本-进阶金属锭
Create.mixing(
  'kubejs:frost_ingot',
  [Fluid.of('kubejs:coolant'),'kubejs:immortal_alloy_ingot']
).superheated()
Create.mixing(
  'kubejs:slag_ingot',
  [Fluid.of('kubejs:oxidizer'),'kubejs:immortal_alloy_ingot','create:scoria']
).superheated()
Create.mixing(
  'kubejs:lumen_ingot',
  [Fluid.of('kubejs:glowing_fluid'),'kubejs:immortal_alloy_ingot']
).superheated()
//计算数质,虚无数质合成
Create.compacting(
  '9x kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk9','kubejs:operator_core']
).superheated()
Create.compacting(
  '8x kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk8','kubejs:operator_core']
).superheated()
Create.compacting(
  '7x kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk7','kubejs:operator_core']
).superheated()
Create.compacting(
  '6x kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk6','kubejs:operator_core']
).superheated()
Create.compacting(
  '5x kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk5','kubejs:operator_core']
).superheated()
Create.compacting(
  '4x kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk4','kubejs:operator_core']
).superheated()
Create.compacting(
  '3x kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk3','kubejs:operator_core']
).superheated()
Create.compacting(
  '2x kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk2','kubejs:operator_core']
).superheated()
Create.compacting(
  'kubejs:calculation_numerical_mass',
  [Fluid.of('kubejs:rainbow_solution'),'kubejs:calculation_core_mk1','kubejs:operator_core']
).superheated()
Create.pressing(
  '2x ae2:mysterious_cube',
  'kubejs:tree3'
)
Create.pressing(
  'ae2:mysterious_cube',
  'kubejs:empty_calculation_core'
)
Create.filling(
  'kubejs:calculation_numerical_mass',
  ['kubejs:virtual_numerical_mass',Fluid.of('kubejs:mutation_fluid')]
)
Create.filling(
  'kubejs:virtual_numerical_mass',
  ['kubejs:calculation_numerical_mass',Fluid.of('kubejs:mutation_fluid')]
)
//虚拟拟合物
Create.mixing(
  'kubejs:virtual_simulant',
  ['kubejs:virtual_numerical_mass',Fluid.of('kubejs:rainbow_solution')]
)
//无尽拟合体
Create.mixing(
  'kubejs:infinite_simulacrum',
  ['kubejs:virtual_numerical_mass','kubejs:calculation_numerical_mass','kubejs:virtual_simulant',Fluid.of('kubejs:rainbow_solution')]
)
//板材和暗光系列物品
Create.pressing(
  'kubejs:refined_sheet',
  'create:refined_radiance'
)
Create.pressing(
  'kubejs:shadow_sheet',
  'create:shadow_steel'
)
Create.pressing(
  'kubejs:chromatic_sheet',
  'create:chromatic_compound'
)
Create.compacting(
  'kubejs:chromatic_shadow_compound_ingot',
  ['kubejs:refined_sheet','kubejs:shadow_sheet','kubejs:chromatic_sheet']
).superheated()
Create.pressing(
  'kubejs:chromatic_shadow_sheet',
  'kubejs:chromatic_shadow_compound_ingot'
)
//压片计算矩阵
Create.pressing(
  'kubejs:pressed_calculation_matrix',
  'kubejs:calculation_matrix_ingot'
)
//补充配方
Create.compacting(
  'kubejs:immortal_alloy_ingot',
  '9x kubejs:immortal_alloy_nugget'
)
//溶液相关
Create.mixing(
  Fluid.of('kubejs:mutation_medium'),
  [Fluid.of('kubejs:chorus_solvent'),'kubejs:mutation_shard']
).superheated()

Create.mixing(
  [Fluid.of('kubejs:coolant'),'minecraft:bucket'],
  ['minecraft:powder_snow_bucket','minecraft:blue_ice']
).superheated()

Create.mixing(
  Fluid.of('kubejs:oxidizer'),
  [Fluid.of('minecraft:lava'),'minecraft:blaze_rod']
).superheated()

Create.mixing(
  Fluid.of('kubejs:glowing_fluid'),
  [Fluid.of('minecraft:water'),'minecraft:glowstone']
).superheated()

Create.mixing(
  Fluid.of('kubejs:chorus_solvent'),
  [Fluid.of('kubejs:mutation_medium'),'minecraft:chorus_fruit']
).superheated()

Create.mixing(
  Fluid.of('kubejs:rainbow_solution'),
  [Fluid.of('kubejs:chorus_solvent'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls'),
  Ingredient.of('#ae2:lumen_paint_balls')
  ]
).superheated()

Create.mixing(
  [Fluid.of('kubejs:glowflow_solution'),'kubejs:glowflow_coupler'],
  [Fluid.of('kubejs:prefab_glowflow_solution'),'kubejs:glowflow_coupler']
).superheated()

  Create.mixing(
    Fluid.of('kubejs:mutation_fluid'),
    [Fluid.of('kubejs:glowflow_solution'),Fluid.of('kubejs:mutation_medium'),'kubejs:mutation_shard']
  ).superheated()

  Create.mixing(
    [Fluid.of('kubejs:mutation_fluid'),CreateItem.of('kubejs:mutation_orb',0.85)],
    [Fluid.of('kubejs:glowflow_solution'),'kubejs:mutation_orb']
  ).superheated()


  Create.mixing(
    Fluid.of('kubejs:prefab_glowflow_solution'),
    [Fluid.of('kubejs:mutation_medium'),Fluid.of('kubejs:rainbow_solution')]
  )

})

ServerEvents.recipes(event => {
  const Create = event.recipes.create
//创造前置
Create.compacting(
  'kubejs:creative_drive_device',
  ['kubejs:creative_calculation_chip','kubejs:creative_core_active','kubejs:adaptive_heavy_creative_item_component']
)
//创造物品合成
//机械动力 创造马达
Create.sequenced_assembly(
  // Outputs:
  [
    CreateItem.of('create:creative_motor', 1)
  ],
  // Input:
  'kubejs:creative_drive_device',
  // Sequence:
  [
    Create.deploying('kubejs:creative_drive_device', ['kubejs:creative_drive_device', 'create:hand_crank',]),
    Create.deploying('kubejs:creative_drive_device', ['kubejs:creative_drive_device', 'create:deployer',]),
    Create.deploying('kubejs:creative_drive_device', ['kubejs:creative_drive_device', 'stone',]),
    Create.deploying('kubejs:creative_drive_device', ['kubejs:creative_drive_device', 'create:deployer',]),
    Create.deploying('kubejs:creative_drive_device', ['kubejs:creative_drive_device', 'create:hand_crank',])
  ]
)
.transitionalItem('kubejs:creative_drive_device')
.loops(1) // Set the number of loops
//应用能源2 创造能源
Create.sequenced_assembly(
  // Outputs:
  [
    CreateItem.of('ae2:creative_energy_cell', 1)
  ],
  // Input:
  'create:creative_motor',
  // Sequence:
  [
    Create.deploying('create:creative_motor', ['create:creative_motor', 'createaddition:alternator',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'ae2:fluix_smart_dense_cable',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'ae2:fluix_smart_dense_cable',])
  ]
)
.transitionalItem('create:creative_motor')
.loops(1) // Set the number of loops
//CCA 创造能源
Create.sequenced_assembly(
  // Outputs:
  [
    CreateItem.of('createaddition:creative_energy', 1)
  ],
  // Input:
  'create:creative_motor',
  // Sequence:
  [
    Create.deploying('create:creative_motor', ['create:creative_motor', 'createaddition:alternator',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'createaddition:electrum_spool',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'createaddition:electrum_spool',])
  ]
)
.transitionalItem('create:creative_motor')
.loops(1) // Set the number of loops
//机械动力:电力学 创造电源
Create.sequenced_assembly(
  // Outputs:
  [
    CreateItem.of('electroenergetics:creative_battery', 1)
  ],
  // Input:
  'create:creative_motor',
  // Sequence:
  [
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:alternator_brushes',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:magnet',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:alternator_rotor',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:magnet',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:iron_bus_spool',])
  ]
)
.transitionalItem('create:creative_motor')
.loops(1) // Set the number of loops
//机械动力:电力学 创造线缆
Create.sequenced_assembly(
  // Outputs:
  [
    CreateItem.of('electroenergetics:creative_wire_spool', 1)
  ],
  // Input:
  'kubejs:creative_drive_device',
  // Sequence:
  [
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:iron_bus_spool',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:iron_rail_spool',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'kubejs:immortal_alloy_ingot',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:iron_rail_spool',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:iron_bus_spool',])
  ]
)
.transitionalItem('create:creative_motor')
.loops(1) // Set the number of loops
//机械动力:电力学 创造电阻
Create.sequenced_assembly(
  // Outputs:
  [
    CreateItem.of('electroenergetics:creative_resistor', 1)
  ],
  // Input:
  'kubejs:creative_drive_device',
  // Sequence:
  [
    Create.deploying('create:creative_motor', ['create:creative_motor', 'kubejs:immortal_alloy_ingot',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:resistor',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'kubejs:immortal_alloy_ingot',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'electroenergetics:resistor',]),
    Create.deploying('create:creative_motor', ['create:creative_motor', 'kubejs:immortal_alloy_ingot',])
  ]
)
.transitionalItem('create:creative_motor')
.loops(1) // Set the number of loops
})