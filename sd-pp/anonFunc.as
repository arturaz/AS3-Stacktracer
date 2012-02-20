package { // anonFunc.as/pkg:root_pkg

import com.tinylabproductions.stacktracer.StacktraceError;

    class Foo { // anonFunc.as/pkg:root_pkg/Foo


              [Bindable (event = "planetBuildingUpgraded")]
      public function getUnitsFacilities(): ListCollectionView
      { try {
         //var constructors:Array = Config.getConstructors(ObjectClass.UNIT);
         var facilities:ListCollectionView = Collections.filter(buildings,
            function(building: Building): Boolean
            { try {
               var result:Boolean = constructors.indexOf(building.type) != -1 &&
               building.state != Building.INACTIVE;
               return result;
            } catch (e: Error) { throw StacktraceError.trace(e, "anonFunc.as/pkg:root_pkg/Foo/Some(getUnitsFacilities)()/None()", {"building": building, "result": result}); } return false; }
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
      } catch (e: Error) { throw StacktraceError.trace(e, "anonFunc.as/pkg:root_pkg/Foo/Some(getUnitsFacilities)()", {"facilities": facilities}); } return null; }
    
private static function __st_static_vars__(localVars: Object): Object {
  return StacktraceError.mergeVars(localVars, null);
}

private function __st_instance_vars__(localVars: Object): Object {
  return StacktraceError.mergeVars(localVars, null);
}
    }
}
