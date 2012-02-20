package {
    class Foo {

              [Bindable (event = "planetBuildingUpgraded")]
      public function getUnitsFacilities(): ListCollectionView
      {
         //var constructors:Array = Config.getConstructors(ObjectClass.UNIT);
         var facilities:ListCollectionView = Collections.filter(buildings,
            function(building: Building): Boolean
            {
               var result:Boolean = constructors.indexOf(building.type) != -1 &&
               building.state != Building.INACTIVE;
               return result;
            }
         );

          /*
           *var foo: Array = []
           */
         facilities.sort = new Sort();
         facilities.sort.fields = [new SortField('constructablePosition', false, false, true),
            new SortField('totalConstructorMod', false, true, true),
            new SortField('id', false, false, true)];
         facilities.refresh();
         return facilities;
      }
    }
}
