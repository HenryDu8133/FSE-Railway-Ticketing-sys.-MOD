ServerEvents.recipes(event => {
    event.remove({output: 'fluidlogistics:infinite_fluid_tank'})
        event.recipes.create.emptying([Fluid.of('ratatouille:egg_yolk',1000),'minecraft:bucket'],'ratatouille:egg_yolk_bucket')
        event.shaped(
        Item.of('simurail:physics_bogey', 1),
          [
            'A'
          ],
          {
            A: 'create:railway_casing'
          }
        )
        event.shaped(
        Item.of('simurail:inverted_physics_bogey', 1),
          [
            'A'
          ],
          {
            A: 'simurail:physics_bogey'
          }
        )
        event.shaped(
        Item.of('simurail:automatic_coupler', 1), // arg 1: 输出
          [
            'BAA'
          ],
          {
            A: 'createdeco:industrial_iron_ingot',
            B: 'minecraft:redstone'
          }
        )
        
    })