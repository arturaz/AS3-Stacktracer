* static functions
* mxml

# handling class vars when they are defined after function

this should be handled by inserting 2 method before class ends:

__st_stat_vars__ - returns Object with all static variables.

__st_inst_vars__ - returns Object with all instance variables, calls
__st_stat_vars__ to include static variables as well.

Each method call inside a class should then use this:

* __st_stat_vars__({...}) if static method
* __st_inst_vars__({...}) if instance method
