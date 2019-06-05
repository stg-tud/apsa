/* BSD 2-Clause License - see OPAL/LICENSE for details. */
import org.opalj.fpcf.properties.allocation_freeness.AllocationFreeMethod;
import org.opalj.fpcf.properties.allocation_freeness.MethodWithAllocations;

/**
 * Tests for the AllocationFreeness property.
 * Note, that the test cases also consider implicitly 
 * thrown exceptions.
 *
 * @author Dominik Helm
 */
public class AllocationFreeness {

    private int field;

    @AllocationFreeMethod("Empty method")
    public void emptyMethod() { }

    @AllocationFreeMethod("Simple getter")
    public int getField() {
        return field;
    }

    @MethodWithAllocations("May throw null pointer exception")
    public int getField(AllocationFreeness other) {
        return other.field;
    }

    @AllocationFreeMethod("Simple setter")
    public void setField(int i){
        field = i;
    }

    @MethodWithAllocations("May throw null pointer exception")
    public void setField(AllocationFreeness other, int i){ other.field = i; }

    @AllocationFreeMethod("Calls method without allocations")
    public void allocationFreeCall(){
        emptyMethod();
    }

    @MethodWithAllocations("Direct allocation")
    public Object getNewObject(){
        return new Object();
    }

    @MethodWithAllocations("Calls method with allocation")
    public Object getNewObjectIndirect(){
        return getNewObject();
    }

    @MethodWithAllocations("Throws directly allocated exception")
    public void throwsExplicitException(){
        throw new RuntimeException();
    }

    @MethodWithAllocations("Throws implicit exception (division by zero)")
    public int divide(int divisor){
        return 10000/divisor;
    }
}
