// 注册所有自定义物品
Platform.mods.kubejs.name = 'FarSight Expanded'
StartupEvents.registry('item', event => {
  // 基础锭
  event.create('frost_ingot')
    .texture('kubejs:item/frost_ingot')
    .displayName('Frost Ingot')
    
  event.create('lumen_ingot')
    .texture('kubejs:item/lumen_ingot')
    .displayName('Lumen Ingot')
    .glow(true)
    
  event.create('slag_ingot')
    .texture('kubejs:item/slag_ingot')
    .displayName('Slag Ingot')
    
  // 异彩化合物系列
  event.create('chromatic_compound_prefab')
    .texture('kubejs:item/chromatic_compound_prefab')
    .displayName('Prefab Chromatic Compound')
    
  event.create('chromatic_compound_normal')
    .texture('kubejs:item/chromatic_compound_normal')
    .displayName('Refined Chromatic compound')
    
  // 板材系列
    event.create('refined_sheet')
    .texture('kubejs:item/refined_sheet')
    .displayName('Refined Sheet')
    .glow(true)
    
  event.create('shadow_sheet')
    .texture('kubejs:item/shadow_sheet')
    .displayName('Shadow Sheet')
    
  event.create('chromatic_sheet')
    .texture('kubejs:item/chromatic_sheet')
    .displayName('Chromatic Sheet')
    
  event.create('chromatic_shadow_compound_ingot')
    .texture('kubejs:item/chromatic_shadow_compound_ingot')
    .displayName('RCS Compound Ingot')
    .glow(true)
    
  event.create('chromatic_shadow_sheet')
    .texture('kubejs:item/chromatic_shadow_sheet')
    .displayName('RCS Sheet')
    .glow(true)

  // 高级中间产物
  event.create('mutation_shard')
    .texture('kubejs:item/mutation_shard')
    .displayName('Mutation Shard')
    
  event.create('mutation_orb')
    .texture('kubejs:item/mutation_orb')
    .displayName('Mutation Orb')
    .glow(true)
    
  event.create('virtual_simulant')
    .texture('kubejs:item/virtual_simulant')
    .displayName('Virtual Simulant')
    
  event.create('infinite_simulacrum')
    .texture('kubejs:item/infinite_simulacrum')
    .displayName('Infinite Simulacrum')
    
  event.create('virtual_numerical_mass')
    .texture('kubejs:item/virtual_numerical_mass')
    .displayName('Virtual Numerical Mass')
    
  event.create('calculation_numerical_mass')
    .texture('kubejs:item/calculation_numerical_mass')
    .displayName('Calculation Numerical Mass')
    
  event.create('infinite_simulation_cube')
    .texture('kubejs:item/infinite_simulation_cube')
    .displayName('Infinite Simulation Cube')
    .glow(true)
    
  event.create('calculation_matrix_ingot')
    .texture('kubejs:item/calculation_matrix_ingot')
    .displayName('Calculation Martix Ingot')
    
  event.create('pressed_calculation_matrix')
    .texture('kubejs:item/pressed_calculation_matrix')
    .displayName('Pressed Calculation Matrix')
    
  event.create('creative_calculation_chip')
    .texture('kubejs:item/creative_calculation_chip')
    .displayName('Creactive Calculation Chip')
    .glow(true)
    
  event.create('creative_core_inactive')
    .texture('kubejs:item/creative_core_inactive')
    .displayName('Inactive Creative Core')
    
  event.create('creative_core_active')
    .texture('kubejs:item/creative_core_active')
    .displayName('Actived Creative Core')
    .glow(true)
    
  event.create('glowflow_coupler')
    .texture('kubejs:item/glowflow_coupler')
    .displayName('Glowflow Coupler')
    .glow(true)
    
    event.create('mutation_energy_core')
    .texture('kubejs:item/mutation_energy_core')
    .displayName('Mutation Energy Core')
    .glow(true)

    event.create('empty_calculation_core')
    .texture('kubejs:item/empty_calculation_core')
    .displayName('Empty Calculation Core')
    .glow(true)

    event.create('operator_core')
    .texture('kubejs:item/operator_core')
    .displayName('Operator Core')
    .glow(true)

    event.create('calculation_core_mk9')
    .texture('kubejs:item/calculation_core_mk9')
    .displayName('MK9 Calculation Core')
    .glow(true)

    event.create('calculation_core_mk8')
    .texture('kubejs:item/calculation_core_mk8')
    .displayName('MK8 Calculation Core')
    .glow(true)

    event.create('calculation_core_mk7')
    .texture('kubejs:item/calculation_core_mk7')
    .displayName('MK7 Calculation Core')
    .glow(true)

    event.create('calculation_core_mk6')
    .texture('kubejs:item/calculation_core_mk6')
    .displayName('MK6 Calculation Core')
    .glow(true)

    event.create('calculation_core_mk5')
    .texture('kubejs:item/calculation_core_mk5')
    .displayName('MK5 Calculation Core')
    .glow(true)

    event.create('calculation_core_mk4')
    .texture('kubejs:item/calculation_core_mk4')
    .displayName('MK4 Calculation Core')
    .glow(true)

    event.create('calculation_core_mk3')
    .texture('kubejs:item/calculation_core_mk3')
    .displayName('MK3 Calculation Core')
    .glow(true)

    event.create('calculation_core_mk2')
    .texture('kubejs:item/calculation_core_mk2')
    .displayName('MK2 Calculation Core')
    .glow(true)

    event.create('calculation_core_mk1')
    .texture('kubejs:item/calculation_core_mk1')
    .displayName('MK1 Calculation Core')
    .glow(true)

    event.create('tree3')
    .texture('kubejs:item/tree3')
    .displayName('TREE(3)')
    .glow(true)

    event.create('item_component')
    .texture('kubejs:item/item_component')
    .displayName('Item Component')

    event.create('heavy_item_component')
    .texture('kubejs:item/heavy_item_component')
    .displayName('Heavy Item Component')

    event.create('super_heavy_item_component')
    .texture('kubejs:item/super_heavy_item_component')
    .displayName('Super Heavy Item Component')

    event.create('adaptive_heavy_creative_item_component')
    .texture('kubejs:item/adaptive_heavy_creative_item_component')
    .displayName('Adaptive Heavy Creative Item Component')

    event.create('creative_drive_device')
    .texture('kubejs:item/creative_drive_device')
    .displayName('Creative Drive Device')
    .glow(true)
//!?活活?!
event.create('arknights_endfield_exe')
.texture('kubejs:item/arknights_endfield_exe')
.displayName('Arknights:Endfield.exe')

event.create('southeast_ascetic_mountain_mp4')
.texture('kubejs:item/southeast_ascetic_mountain_mp4')
.displayName('Southeast Ascetic Mountain.mp4')

    //序列装配-未完成时的产物
    event.create('uncalculationed_core')
    .texture('kubejs:item/calculation_core_mk1')
    .displayName('Uncalculationd Core')
    .glow(true)
    
    event.create('creative_core_activing')
    .texture('kubejs:item/creative_core_activing')
    .displayName('Activing Creative Core')
    .glow(true)

    event.create('incomplete_creative_calculation_chip')
    .texture('kubejs:item/incomplete_creative_calculation_chip')
    .displayName('Incomplete Creative Calculation Chip')
    .glow(true)
    //占位符
    event.create('placeholder')
    .texture('kubejs:item/placeholder')
    .displayName('Placeholder')
    .glow(true)

});

// 注册流体
StartupEvents.registry('fluid', event => {
  // 彩虹溶液
    const rainbow_solution = event
    .create('rainbow_solution')
    .stillTexture('kubejs:fluid/rainbow_solution_still')
    .flowingTexture('kubejs:fluid/rainbow_solution_flow')
    .displayName('Rainbow Solution')
    rainbow_solution.bucketItem;
    
  // 流光溶液
    const glowflow_solution = event
    .create('glowflow_solution')
    .stillTexture('kubejs:fluid/glowflow_solution_still')
    .flowingTexture('kubejs:fluid/glowflow_solution_flow')
    .displayName('Glowflow Solution')
    glowflow_solution.bucketItem;
    
  // 嬗变流体
    const mutation_fluid = event
    .create('mutation_fluid')
    .stillTexture('kubejs:fluid/mutation_fluid_still')
    .flowingTexture('kubejs:fluid/mutation_fluid_flow')
    .displayName('Mutation Fluid')
    mutation_fluid.bucketItem;
    
  // 紫颂溶剂
    const chorus_solvent = event
    .create('chorus_solvent')
    .stillTexture('kubejs:fluid/chorus_solvent_still')
    .flowingTexture('kubejs:fluid/chorus_solvent_flow')
    .displayName('Chorus Solvent')
    chorus_solvent.bucketItem;
    
  // 嬗变介质
    const mutation_medium = event
    .create('mutation_medium')
    .stillTexture('kubejs:fluid/mutation_medium_still')
    .flowingTexture('kubejs:fluid/mutation_medium_flow')
    .displayName('Mutation Medium')
    mutation_medium.bucketItem;
    
  // 荧光溶液
    const glowing_fluid = event
    .create('glowing_fluid')
    .stillTexture('kubejs:fluid/glowing_fluid_still')
    .flowingTexture('kubejs:fluid/glowing_fluid_flow')
    .displayName('Glowing Fluid')
    glowing_fluid.bucketItem;

      // 预制流光溶液
      const prefab_glowflow_solution = event
      .create('prefab_glowflow_solution')
      .stillTexture('kubejs:fluid/prefab_glowflow_solution_still')
      .flowingTexture('kubejs:fluid/prefab_glowflow_solution_flow')
      .displayName('Glowflow Solution')
      prefab_glowflow_solution.bucketItem;
});