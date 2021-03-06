/*
 * Copyright 2011 Goldman Sachs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gs.collections.impl.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.gs.collections.api.block.function.Function;
import com.gs.collections.api.block.function.Function2;
import com.gs.collections.api.block.function.Function3;
import com.gs.collections.api.block.predicate.Predicate2;
import com.gs.collections.api.block.procedure.Procedure2;
import com.gs.collections.api.block.procedure.primitive.ObjectIntProcedure;
import com.gs.collections.api.list.MutableList;
import com.gs.collections.api.list.primitive.MutableBooleanList;
import com.gs.collections.api.list.primitive.MutableByteList;
import com.gs.collections.api.list.primitive.MutableCharList;
import com.gs.collections.api.list.primitive.MutableDoubleList;
import com.gs.collections.api.list.primitive.MutableFloatList;
import com.gs.collections.api.list.primitive.MutableIntList;
import com.gs.collections.api.list.primitive.MutableLongList;
import com.gs.collections.api.list.primitive.MutableShortList;
import com.gs.collections.api.multimap.MutableMultimap;
import com.gs.collections.api.partition.list.PartitionMutableList;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.api.tuple.Twin;
import com.gs.collections.impl.block.factory.Comparators;
import com.gs.collections.impl.block.factory.Functions;
import com.gs.collections.impl.block.factory.ObjectIntProcedures;
import com.gs.collections.impl.block.factory.Predicates;
import com.gs.collections.impl.block.factory.Predicates2;
import com.gs.collections.impl.block.factory.PrimitiveFunctions;
import com.gs.collections.impl.block.factory.Procedures2;
import com.gs.collections.impl.block.function.AddFunction;
import com.gs.collections.impl.block.function.MaxSizeFunction;
import com.gs.collections.impl.block.function.MinSizeFunction;
import com.gs.collections.impl.block.procedure.CollectionAddProcedure;
import com.gs.collections.impl.factory.Lists;
import com.gs.collections.impl.list.Interval;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.list.mutable.primitive.BooleanArrayList;
import com.gs.collections.impl.list.mutable.primitive.ByteArrayList;
import com.gs.collections.impl.list.mutable.primitive.CharArrayList;
import com.gs.collections.impl.list.mutable.primitive.DoubleArrayList;
import com.gs.collections.impl.list.mutable.primitive.FloatArrayList;
import com.gs.collections.impl.list.mutable.primitive.IntArrayList;
import com.gs.collections.impl.list.mutable.primitive.LongArrayList;
import com.gs.collections.impl.list.mutable.primitive.ShortArrayList;
import com.gs.collections.impl.math.IntegerSum;
import com.gs.collections.impl.math.Sum;
import com.gs.collections.impl.multimap.list.FastListMultimap;
import com.gs.collections.impl.test.Verify;
import com.gs.collections.impl.tuple.Tuples;
import org.junit.Assert;
import org.junit.Test;

import static com.gs.collections.impl.factory.Iterables.*;

/**
 * JUnit test for {@link ArrayListIterate}.
 */
public class ArrayListIterateTest
{
    private static final int OVER_OPTIMIZED_LIMIT = 101;

    private static final class ThisIsNotAnArrayList<T>
            extends ArrayList<T>
    {
        private static final long serialVersionUID = 1L;

        private ThisIsNotAnArrayList(Collection<? extends T> collection)
        {
            super(collection);
        }
    }

    @Test
    public void testThisIsNotAnArrayList()
    {
        ThisIsNotAnArrayList<Integer> undertest = new ThisIsNotAnArrayList<Integer>(FastList.newListWith(1, 2, 3));
        Assert.assertNotSame(undertest.getClass(), ArrayList.class);
    }

    @Test
    public void sortOnListWithLessThan10Elements()
    {
        ArrayList<Integer> integers = this.newArrayList(2, 3, 4, 1, 5, 7, 6, 9, 8);
        Verify.assertStartsWith(ArrayListIterate.sortThis(integers), 1, 2, 3, 4, 5, 6, 7, 8, 9);

        ArrayList<Integer> integers2 = this.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Verify.assertStartsWith(
                ArrayListIterate.sortThis(integers2, Collections.<Integer>reverseOrder()),
                9, 8, 7, 6, 5, 4, 3, 2, 1);

        ArrayList<Integer> integers3 = this.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Verify.assertStartsWith(ArrayListIterate.sortThis(integers3), 1, 2, 3, 4, 5, 6, 7, 8, 9);

        ArrayList<Integer> integers4 = this.newArrayList(9, 8, 7, 6, 5, 4, 3, 2, 1);
        Verify.assertStartsWith(ArrayListIterate.sortThis(integers4), 1, 2, 3, 4, 5, 6, 7, 8, 9);

        ThisIsNotAnArrayList<Integer> arrayListThatIsnt = new ThisIsNotAnArrayList<Integer>(FastList.newListWith(9, 8, 7, 6, 5, 4, 3, 2, 1));
        Verify.assertStartsWith(ArrayListIterate.sortThis(arrayListThatIsnt), 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Test
    public void sortingWitoutAccessToInternalArray()
    {
        ThisIsNotAnArrayList<Integer> arrayListThatIsnt = new ThisIsNotAnArrayList<Integer>(FastList.newListWith(5, 3, 4, 1, 2));
        Verify.assertStartsWith(ArrayListIterate.sortThis(arrayListThatIsnt, Comparators.naturalOrder()), 1, 2, 3, 4, 5);
    }

    @Test
    public void copyToArray()
    {
        ThisIsNotAnArrayList<Integer> notAnArrayList = this.newNotAnArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Integer[] target1 = {1, 2, null, null};
        ArrayListIterate.toArray(notAnArrayList, target1, 2, 2);
        Assert.assertArrayEquals(target1, new Integer[]{1, 2, 1, 2});

        ArrayList<Integer> arrayList = this.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Integer[] target2 = {1, 2, null, null};
        ArrayListIterate.toArray(arrayList, target2, 2, 2);
        Assert.assertArrayEquals(target2, new Integer[]{1, 2, 1, 2});
    }

    @Test
    public void sortOnListWithMoreThan10Elements()
    {
        ArrayList<Integer> integers = this.newArrayList(2, 3, 4, 1, 5, 7, 6, 8, 10, 9);
        Verify.assertStartsWith(ArrayListIterate.sortThis(integers), 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        ArrayList<Integer> integers2 = this.newArrayList(1, 2, 3, 4, 5, 6, 7, 8);
        Verify.assertStartsWith(
                ArrayListIterate.sortThis(integers2, Collections.<Integer>reverseOrder()),
                8, 7, 6, 5, 4, 3, 2, 1);

        ArrayList<Integer> integers3 = this.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Verify.assertStartsWith(ArrayListIterate.sortThis(integers3), 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    public void forEachUsingFromTo()
    {
        final ArrayList<Integer> integers = Interval.oneTo(5).addAllTo(new ArrayList<Integer>());
        ArrayList<Integer> results = new ArrayList<Integer>();
        ArrayListIterate.forEach(integers, 0, 4, CollectionAddProcedure.on(results));
        Assert.assertEquals(integers, results);
        MutableList<Integer> reverseResults = Lists.mutable.of();
        final CollectionAddProcedure<Integer> procedure = CollectionAddProcedure.on(reverseResults);
        ArrayListIterate.forEach(integers, 4, 0, procedure);
        Assert.assertEquals(ListIterate.reverseThis(integers), reverseResults);
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                ArrayListIterate.forEach(integers, 4, -1, procedure);
            }
        });
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                ArrayListIterate.forEach(integers, -1, 4, procedure);
            }
        });
    }

    @Test
    public void forEachUsingFromToWithOptimisable()
    {
        ArrayList<Integer> expected = Interval.oneTo(5).addAllTo(new ArrayList<Integer>());
        final ArrayList<Integer> optimisableList = Interval.oneTo(105).addAllTo(new ArrayList<Integer>());
        ArrayList<Integer> results = new ArrayList<Integer>();
        ArrayListIterate.forEach(optimisableList, 0, 4, CollectionAddProcedure.on(results));
        Assert.assertEquals(expected, results);
        MutableList<Integer> reverseResults = Lists.mutable.of();
        final CollectionAddProcedure<Integer> procedure = CollectionAddProcedure.on(reverseResults);
        ArrayListIterate.forEach(optimisableList, 4, 0, procedure);
        Assert.assertEquals(ListIterate.reverseThis(expected), reverseResults);
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                ArrayListIterate.forEach(optimisableList, 104, -1, procedure);
            }
        });
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                ArrayListIterate.forEach(optimisableList, -1, 4, procedure);
            }
        });
    }

    @Test
    public void forEachWithIndexUsingFromTo()
    {
        final ArrayList<Integer> integers = Interval.oneTo(5).addAllTo(new ArrayList<Integer>());
        ArrayList<Integer> results = new ArrayList<Integer>();
        ArrayListIterate.forEachWithIndex(integers, 0, 4, ObjectIntProcedures.fromProcedure(CollectionAddProcedure.on(results)));
        Assert.assertEquals(integers, results);
        MutableList<Integer> reverseResults = Lists.mutable.of();
        final ObjectIntProcedure<Integer> objectIntProcedure = ObjectIntProcedures.fromProcedure(CollectionAddProcedure.on(reverseResults));
        ArrayListIterate.forEachWithIndex(integers, 4, 0, objectIntProcedure);
        Assert.assertEquals(ListIterate.reverseThis(integers), reverseResults);
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                ArrayListIterate.forEachWithIndex(integers, 4, -1, objectIntProcedure);
            }
        });
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                ArrayListIterate.forEachWithIndex(integers, -1, 4, objectIntProcedure);
            }
        });
    }

    @Test
    public void forEachWithIndexUsingFromToWithOptimisableList()
    {
        ArrayList<Integer> optimisableList = Interval.oneTo(105).addAllTo(new ArrayList<Integer>());
        final ArrayList<Integer> expected = Interval.oneTo(105).addAllTo(new ArrayList<Integer>());
        ArrayList<Integer> results = new ArrayList<Integer>();
        ArrayListIterate.forEachWithIndex(optimisableList, 0, 104, ObjectIntProcedures.fromProcedure(CollectionAddProcedure.on(results)));
        Assert.assertEquals(expected, results);
        MutableList<Integer> reverseResults = Lists.mutable.of();
        final ObjectIntProcedure<Integer> objectIntProcedure = ObjectIntProcedures.fromProcedure(CollectionAddProcedure.on(reverseResults));
        ArrayListIterate.forEachWithIndex(expected, 104, 0, objectIntProcedure);
        Assert.assertEquals(ListIterate.reverseThis(expected), reverseResults);
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                ArrayListIterate.forEachWithIndex(expected, 104, -1, objectIntProcedure);
            }
        });
        Verify.assertThrows(IndexOutOfBoundsException.class, new Runnable()
        {
            public void run()
            {
                ArrayListIterate.forEachWithIndex(expected, -1, 104, objectIntProcedure);
            }
        });
    }

    @Test
    public void reverseForEach()
    {
        ArrayList<Integer> integers = Interval.oneTo(5).addAllTo(new ArrayList<Integer>());
        MutableList<Integer> reverseResults = Lists.mutable.of();
        ArrayListIterate.reverseForEach(integers, CollectionAddProcedure.on(reverseResults));
        Assert.assertEquals(ListIterate.reverseThis(integers), reverseResults);
    }

    @Test
    public void reverseForEach_emptyList()
    {
        ArrayList<Integer> integers = new ArrayList<Integer>();
        MutableList<Integer> results = Lists.mutable.of();
        ArrayListIterate.reverseForEach(integers, CollectionAddProcedure.on(results));
        Assert.assertEquals(integers, results);
    }

    @Test
    public void injectInto()
    {
        ArrayList<Integer> list = this.newArrayList(1, 2, 3);
        Assert.assertEquals(Integer.valueOf(1 + 1 + 2 + 3),
                ArrayListIterate.injectInto(1, list, AddFunction.INTEGER));
    }

    @Test
    public void injectIntoOver100()
    {
        ArrayList<Integer> list = this.oneHundredAndOneOnes();
        Assert.assertEquals(Integer.valueOf(102), ArrayListIterate.injectInto(1, list, AddFunction.INTEGER));
    }

    @Test
    public void injectIntoDoubleOver100()
    {
        ArrayList<Integer> list = this.oneHundredAndOneOnes();
        Assert.assertEquals(102.0, ArrayListIterate.injectInto(1.0d, list, AddFunction.INTEGER_TO_DOUBLE), 0.0001);
    }

    private ArrayList<Integer> oneHundredAndOneOnes()
    {
        return new ArrayList<Integer>(Collections.nCopies(101, 1));
    }

    @Test
    public void injectIntoIntegerOver100()
    {
        ArrayList<Integer> list = this.oneHundredAndOneOnes();
        Assert.assertEquals(102, ArrayListIterate.injectInto(1, list, AddFunction.INTEGER_TO_INT));
    }

    @Test
    public void injectIntoLongOver100()
    {
        ArrayList<Integer> list = this.oneHundredAndOneOnes();
        Assert.assertEquals(102, ArrayListIterate.injectInto(1L, list, AddFunction.INTEGER_TO_LONG));
    }

    @Test
    public void injectIntoDouble()
    {
        ArrayList<Double> list = new ArrayList<Double>();
        list.add(1.0);
        list.add(2.0);
        list.add(3.0);
        Assert.assertEquals(new Double(7.0),
                ArrayListIterate.injectInto(1.0, list, AddFunction.DOUBLE));
    }

    @Test
    public void injectIntoString()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        Assert.assertEquals("0123", ArrayListIterate.injectInto("0", list, AddFunction.STRING));
    }

    @Test
    public void injectIntoMaxString()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("1");
        list.add("12");
        list.add("123");
        Assert.assertEquals(Integer.valueOf(3), ArrayListIterate.injectInto(Integer.MIN_VALUE, list, MaxSizeFunction.STRING));
    }

    @Test
    public void injectIntoMinString()
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("1");
        list.add("12");
        list.add("123");
        Assert.assertEquals(Integer.valueOf(1), ArrayListIterate.injectInto(Integer.MAX_VALUE, list, MinSizeFunction.STRING));
    }

    @Test
    public void collect()
    {
        ArrayList<Boolean> list = new ArrayList<Boolean>();
        list.add(Boolean.TRUE);
        list.add(Boolean.FALSE);
        list.add(Boolean.TRUE);
        list.add(Boolean.TRUE);
        list.add(Boolean.FALSE);
        list.add(null);
        list.add(null);
        list.add(Boolean.FALSE);
        list.add(Boolean.TRUE);
        list.add(null);
        ArrayList<String> newCollection = ArrayListIterate.collect(list, Functions.getToString());
        //List<String> newCollection = ArrayListIterate.collect(list, ArrayListIterateTest.TO_STRING_FUNCTION);
        Verify.assertSize(10, newCollection);
        Verify.assertContainsAll(newCollection, "null", "false", "true");
    }

    @Test
    public void collectBoolean()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(-1);
        list.add(0);
        list.add(4);
        MutableBooleanList actual = ArrayListIterate.collectBoolean(list, PrimitiveFunctions.integerIsPositive());
        Assert.assertEquals(BooleanArrayList.newListWith(false, false, true), actual);
    }

    @Test
    public void collectBooleanOverOptimizeLimit()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.zeroTo(OVER_OPTIMIZED_LIMIT));
        MutableBooleanList actual = ArrayListIterate.collectBoolean(list, PrimitiveFunctions.integerIsPositive());
        BooleanArrayList expected = new BooleanArrayList(list.size());
        expected.add(false);
        for (int i = 1; i <= OVER_OPTIMIZED_LIMIT; i++)
        {
            expected.add(true);
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void collectByte()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(-1);
        list.add(0);
        list.add(4);
        MutableByteList actual = ArrayListIterate.collectByte(list, PrimitiveFunctions.unboxIntegerToByte());
        Assert.assertEquals(ByteArrayList.newListWith((byte) -1, (byte) 0, (byte) 4), actual);
    }

    @Test
    public void collectByteOverOptimizeLimit()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.zeroTo(OVER_OPTIMIZED_LIMIT));
        MutableByteList actual = ArrayListIterate.collectByte(list, PrimitiveFunctions.unboxIntegerToByte());
        ByteArrayList expected = new ByteArrayList(list.size());
        for (int i = 0; i <= OVER_OPTIMIZED_LIMIT; i++)
        {
            expected.add((byte) i);
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void collectChar()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(-1);
        list.add(0);
        list.add(4);
        MutableCharList actual = ArrayListIterate.collectChar(list, PrimitiveFunctions.unboxIntegerToChar());
        Assert.assertEquals(CharArrayList.newListWith((char) -1, (char) 0, (char) 4), actual);
    }

    @Test
    public void collectCharOverOptimizeLimit()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.zeroTo(OVER_OPTIMIZED_LIMIT));
        MutableCharList actual = ArrayListIterate.collectChar(list, PrimitiveFunctions.unboxIntegerToChar());
        CharArrayList expected = new CharArrayList(list.size());
        for (int i = 0; i <= OVER_OPTIMIZED_LIMIT; i++)
        {
            expected.add((char) i);
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void collectDouble()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(-1);
        list.add(0);
        list.add(4);
        MutableDoubleList actual = ArrayListIterate.collectDouble(list, PrimitiveFunctions.unboxIntegerToDouble());
        Assert.assertEquals(DoubleArrayList.newListWith(-1.0d, 0.0d, 4.0d), actual);
    }

    @Test
    public void collectDoubleOverOptimizeLimit()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.zeroTo(OVER_OPTIMIZED_LIMIT));
        MutableDoubleList actual = ArrayListIterate.collectDouble(list, PrimitiveFunctions.unboxIntegerToDouble());
        DoubleArrayList expected = new DoubleArrayList(list.size());
        for (int i = 0; i <= OVER_OPTIMIZED_LIMIT; i++)
        {
            expected.add((double) i);
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void collectFloat()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(-1);
        list.add(0);
        list.add(4);
        MutableFloatList actual = ArrayListIterate.collectFloat(list, PrimitiveFunctions.unboxIntegerToFloat());
        Assert.assertEquals(FloatArrayList.newListWith(-1.0f, 0.0f, 4.0f), actual);
    }

    @Test
    public void collectFloatOverOptimizeLimit()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.zeroTo(OVER_OPTIMIZED_LIMIT));
        MutableFloatList actual = ArrayListIterate.collectFloat(list, PrimitiveFunctions.unboxIntegerToFloat());
        FloatArrayList expected = new FloatArrayList(list.size());
        for (int i = 0; i <= OVER_OPTIMIZED_LIMIT; i++)
        {
            expected.add((float) i);
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void collectInt()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(-1);
        list.add(0);
        list.add(4);
        MutableIntList actual = ArrayListIterate.collectInt(list, PrimitiveFunctions.unboxIntegerToInt());
        Assert.assertEquals(IntArrayList.newListWith(-1, 0, 4), actual);
    }

    @Test
    public void collectIntOverOptimizeLimit()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.zeroTo(OVER_OPTIMIZED_LIMIT));
        MutableIntList actual = ArrayListIterate.collectInt(list, PrimitiveFunctions.unboxIntegerToInt());
        IntArrayList expected = new IntArrayList(list.size());
        for (int i = 0; i <= OVER_OPTIMIZED_LIMIT; i++)
        {
            expected.add(i);
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void collectLong()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(-1);
        list.add(0);
        list.add(4);
        MutableLongList actual = ArrayListIterate.collectLong(list, PrimitiveFunctions.unboxIntegerToLong());
        Assert.assertEquals(LongArrayList.newListWith(-1L, 0L, 4L), actual);
    }

    @Test
    public void collectLongOverOptimizeLimit()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.zeroTo(OVER_OPTIMIZED_LIMIT));
        MutableLongList actual = ArrayListIterate.collectLong(list, PrimitiveFunctions.unboxIntegerToLong());
        LongArrayList expected = new LongArrayList(list.size());
        for (int i = 0; i <= OVER_OPTIMIZED_LIMIT; i++)
        {
            expected.add((long) i);
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void collectShort()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(-1);
        list.add(0);
        list.add(4);
        MutableShortList actual = ArrayListIterate.collectShort(list, PrimitiveFunctions.unboxIntegerToShort());
        Assert.assertEquals(ShortArrayList.newListWith((short) -1, (short) 0, (short) 4), actual);
    }

    @Test
    public void collectShortOverOptimizeLimit()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.zeroTo(OVER_OPTIMIZED_LIMIT));
        MutableShortList actual = ArrayListIterate.collectShort(list, PrimitiveFunctions.unboxIntegerToShort());
        ShortArrayList expected = new ShortArrayList(list.size());
        for (int i = 0; i <= OVER_OPTIMIZED_LIMIT; i++)
        {
            expected.add((short) i);
        }
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void collectOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        ArrayList<Class<?>> newCollection = ArrayListIterate.collect(list, Functions.getToClass());
        Verify.assertSize(101, newCollection);
        Verify.assertContains(Integer.class, newCollection);
    }

    private ArrayList<Integer> getIntegerList()
    {
        return new ArrayList<Integer>(Interval.toReverseList(1, 5));
    }

    private ArrayList<Integer> getOver100IntegerList()
    {
        return new ArrayList<Integer>(Interval.toReverseList(1, 105));
    }

    @Test
    public void forEachWithIndex()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Iterate.sortThis(list);
        ArrayListIterate.forEachWithIndex(list, new ObjectIntProcedure<Integer>()
        {
            public void value(Integer object, int index)
            {
                Assert.assertEquals(index, object - 1);
            }
        });
    }

    @Test
    public void forEachWithIndexOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Iterate.sortThis(list);
        ArrayListIterate.forEachWithIndex(list, new ObjectIntProcedure<Integer>()
        {
            public void value(Integer object, int index)
            {
                Assert.assertEquals(index, object - 1);
            }
        });
    }

    @Test
    public void forEach()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Iterate.sortThis(list);
        MutableList<Integer> result = Lists.mutable.of();
        ArrayListIterate.forEach(list, CollectionAddProcedure.<Integer>on(result));
        Verify.assertListsEqual(list, result);
    }

    @Test
    public void forEachOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Iterate.sortThis(list);
        FastList<Integer> result = FastList.newList(101);
        ArrayListIterate.forEach(list, CollectionAddProcedure.<Integer>on(result));
        Verify.assertListsEqual(list, result);
    }

    @Test
    public void forEachWith()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Iterate.sortThis(list);
        MutableList<Integer> result = Lists.mutable.of();
        ArrayListIterate.forEachWith(list,
                Procedures2.fromProcedure(CollectionAddProcedure.<Integer>on(result)),
                null);
        Verify.assertListsEqual(list, result);
    }

    @Test
    public void forEachWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Iterate.sortThis(list);
        MutableList<Integer> result = FastList.newList(101);
        ArrayListIterate.forEachWith(list,
                Procedures2.fromProcedure(CollectionAddProcedure.<Integer>on(result)),
                null);
        Verify.assertListsEqual(list, result);
    }

    @Test
    public void forEachInBoth()
    {
        final MutableList<Pair<String, String>> list = Lists.mutable.of();
        ArrayList<String> list1 = new ArrayList<String>(mList("1", "2"));
        ArrayList<String> list2 = new ArrayList<String>(mList("a", "b"));
        ArrayListIterate.forEachInBoth(list1, list2,
                new Procedure2<String, String>()
                {
                    public void value(String argument1, String argument2)
                    {
                        list.add(Tuples.twin(argument1, argument2));
                    }
                });

        Assert.assertEquals(FastList.newListWith(Tuples.twin("1", "a"), Tuples.twin("2", "b")), list);
    }

    @Test
    public void detect()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertEquals(Integer.valueOf(1), ArrayListIterate.detect(list, Predicates.equal(1)));
        //noinspection CachedNumberConstructorCall,UnnecessaryBoxing
        ArrayList<Integer> list2 =
                this.newArrayList(1, new Integer(2), 2);  // test relies on having a unique instance of "2"
        Assert.assertSame(list2.get(1), ArrayListIterate.detect(list2, Predicates.equal(2)));
    }

    @Test
    public void detectOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertEquals(Integer.valueOf(1), ArrayListIterate.detect(list, Predicates.equal(1)));
    }

    @Test
    public void detectWith()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertEquals(Integer.valueOf(1), ArrayListIterate.detectWith(list, Predicates2.equal(), 1));
        //noinspection CachedNumberConstructorCall,UnnecessaryBoxing
        ArrayList<Integer> list2 =
                this.newArrayList(1, new Integer(2), 2);  // test relies on having a unique instance of "2"
        Assert.assertSame(list2.get(1), ArrayListIterate.detectWith(list2, Predicates2.equal(), 2));
    }

    @Test
    public void detectWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertEquals(Integer.valueOf(1), ArrayListIterate.detectWith(list, Predicates2.equal(), 1));
    }

    @Test
    public void detectIfNone()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertEquals(Integer.valueOf(7), ArrayListIterate.detectIfNone(list, Predicates.equal(6), 7));
        Assert.assertEquals(Integer.valueOf(2), ArrayListIterate.detectIfNone(list, Predicates.equal(2), 7));
    }

    @Test
    public void detectIfNoneOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertNull(ArrayListIterate.detectIfNone(list, Predicates.equal(102), null));
    }

    @Test
    public void detectWithIfNone()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertEquals(Integer.valueOf(7), ArrayListIterate.detectWithIfNone(list, Predicates2.equal(), 6, 7));
        Assert.assertEquals(Integer.valueOf(2), ArrayListIterate.detectWithIfNone(list, Predicates2.equal(), 2, 7));
    }

    @Test
    public void detectWithIfNoneOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertNull(ArrayListIterate.detectWithIfNone(list, Predicates2.equal(), 102, null));
    }

    @Test
    public void select()
    {
        ArrayList<Integer> list = this.getIntegerList();
        ArrayList<Integer> results = ArrayListIterate.select(list, Predicates.instanceOf(Integer.class));
        Verify.assertSize(5, results);
    }

    @Test
    public void selectOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        ArrayList<Integer> results = ArrayListIterate.select(list, Predicates.instanceOf(Integer.class));
        Verify.assertSize(101, results);
    }

    @Test
    public void selectWith()
    {
        ArrayList<Integer> list = this.getIntegerList();
        ArrayList<Integer> results = ArrayListIterate.selectWith(list, Predicates2.instanceOf(), Integer.class);
        Verify.assertSize(5, results);
    }

    @Test
    public void selectWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        ArrayList<Integer> results = ArrayListIterate.selectWith(list, Predicates2.instanceOf(), Integer.class);
        Verify.assertSize(101, results);
    }

    @Test
    public void reject()
    {
        ArrayList<Integer> list = this.getIntegerList();
        ArrayList<Integer> results = ArrayListIterate.reject(list, Predicates.instanceOf(Integer.class));
        Verify.assertEmpty(results);
    }

    @Test
    public void rejectOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        List<Integer> results = ArrayListIterate.reject(list, Predicates.instanceOf(Integer.class));
        Verify.assertEmpty(results);
    }

    @Test
    public void distinct()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.addAll(FastList.newListWith(9, 4, 7, 7, 5, 6, 2, 4));
        List<Integer> result = ArrayListIterate.distinct(list);
        Verify.assertListsEqual(FastList.newListWith(9, 4, 7, 5, 6, 2), result);
        ArrayList<Integer> target = new ArrayList<Integer>();
        ArrayListIterate.distinct(list, target);
        Verify.assertListsEqual(FastList.newListWith(9, 4, 7, 5, 6, 2), target);
        Verify.assertSize(8, list);
    }

    @Test
    public void selectInstancesOfOver100()
    {
        ArrayList<Number> list = new ArrayList<Number>(Interval.oneTo(101));
        list.add(102.0);
        MutableList<Double> results = ArrayListIterate.selectInstancesOf(list, Double.class);
        Assert.assertEquals(iList(102.0), results);
    }

    public static final class CollectionCreator
    {
        private final int data;

        private CollectionCreator(int data)
        {
            this.data = data;
        }

        public Collection<Integer> getCollection()
        {
            return FastList.newListWith(this.data, this.data);
        }
    }

    @Test
    public void flatCollect()
    {
        ArrayList<CollectionCreator> list = new ArrayList<CollectionCreator>();
        list.add(new CollectionCreator(1));
        list.add(new CollectionCreator(2));

        Function<CollectionCreator, Collection<Integer>> flatteningFunction = new Function<CollectionCreator, Collection<Integer>>()
        {
            public Collection<Integer> valueOf(CollectionCreator object)
            {
                return object.getCollection();
            }
        };
        List<Integer> results1 = ArrayListIterate.flatCollect(list, flatteningFunction);
        Verify.assertListsEqual(FastList.newListWith(1, 1, 2, 2), results1);

        MutableList<Integer> target1 = Lists.mutable.of();
        MutableList<Integer> results2 = ArrayListIterate.flatCollect(list, flatteningFunction, target1);
        Assert.assertSame(results2, target1);

        Verify.assertListsEqual(FastList.newListWith(1, 1, 2, 2), results2);
    }

    @Test
    public void rejectWith()
    {
        ArrayList<Integer> list = this.getIntegerList();
        ArrayList<Integer> results = ArrayListIterate.rejectWith(list, Predicates2.instanceOf(), Integer.class);
        Verify.assertEmpty(results);
    }

    @Test
    public void rejectWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        ArrayList<Integer> results = ArrayListIterate.rejectWith(list, Predicates2.instanceOf(), Integer.class);
        Verify.assertEmpty(results);
    }

    @Test
    public void selectAndRejectWith()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Twin<MutableList<Integer>> result =
                ArrayListIterate.selectAndRejectWith(list, Predicates2.in(), Lists.immutable.of(1));
        Verify.assertSize(1, result.getOne());
        Verify.assertSize(4, result.getTwo());
    }

    @Test
    public void selectAndRejectWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Twin<MutableList<Integer>> result =
                ArrayListIterate.selectAndRejectWith(list, Predicates2.in(), Lists.immutable.of(1));
        Verify.assertSize(1, result.getOne());
        Verify.assertSize(100, result.getTwo());
    }

    @Test
    public void partitionOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        PartitionMutableList<Integer> result =
                ArrayListIterate.partition(list, Predicates.in(Lists.immutable.of(1)));
        Verify.assertSize(1, result.getSelected());
        Verify.assertSize(100, result.getRejected());
    }

    @Test
    public void anySatisfyWith()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertTrue(ArrayListIterate.anySatisfyWith(list, Predicates2.instanceOf(), Integer.class));
        Assert.assertFalse(ArrayListIterate.anySatisfyWith(list, Predicates2.instanceOf(), Double.class));
    }

    @Test
    public void anySatisfy()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertTrue(ArrayListIterate.anySatisfy(list, Predicates.instanceOf(Integer.class)));
        Assert.assertFalse(ArrayListIterate.anySatisfy(list, Predicates.instanceOf(Double.class)));
    }

    @Test
    public void anySatisfyWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertTrue(ArrayListIterate.anySatisfyWith(list, Predicates2.instanceOf(), Integer.class));
        Assert.assertFalse(ArrayListIterate.anySatisfyWith(list, Predicates2.instanceOf(), Double.class));
    }

    @Test
    public void anySatisfyOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertTrue(ArrayListIterate.anySatisfy(list, Predicates.instanceOf(Integer.class)));
        Assert.assertFalse(ArrayListIterate.anySatisfy(list, Predicates.instanceOf(Double.class)));
    }

    @Test
    public void allSatisfyWith()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertTrue(ArrayListIterate.allSatisfyWith(list, Predicates2.instanceOf(), Integer.class));
        Predicate2<Integer, Integer> greaterThanPredicate = Predicates2.greaterThan();
        Assert.assertFalse(ArrayListIterate.allSatisfyWith(list, greaterThanPredicate, 2));
    }

    @Test
    public void allSatisfy()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertTrue(ArrayListIterate.allSatisfy(list, Predicates.instanceOf(Integer.class)));
        Assert.assertFalse(ArrayListIterate.allSatisfy(list, Predicates.greaterThan(2)));
    }

    @Test
    public void allSatisfyWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertTrue(ArrayListIterate.allSatisfyWith(list, Predicates2.instanceOf(), Integer.class));
        Predicate2<Integer, Integer> greaterThanPredicate = Predicates2.greaterThan();
        Assert.assertFalse(ArrayListIterate.allSatisfyWith(list, greaterThanPredicate, 2));
    }

    @Test
    public void allSatisfyOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertTrue(ArrayListIterate.allSatisfy(list, Predicates.instanceOf(Integer.class)));
        Assert.assertFalse(ArrayListIterate.allSatisfy(list, Predicates.greaterThan(2)));
    }

    @Test
    public void noneSatisfyOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertFalse(ArrayListIterate.noneSatisfy(list, Predicates.instanceOf(Integer.class)));
        Assert.assertTrue(ArrayListIterate.noneSatisfy(list, Predicates.greaterThan(150)));
    }

    @Test
    public void noneSatisfyWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertFalse(ArrayListIterate.noneSatisfyWith(list, Predicates2.instanceOf(), Integer.class));
        Predicate2<Integer, Integer> greaterThanPredicate = Predicates2.greaterThan();
        Assert.assertTrue(ArrayListIterate.noneSatisfyWith(list, greaterThanPredicate, 150));
    }

    @Test
    public void countWith()
    {
        ArrayList<Integer> list = this.getIntegerList();
        Assert.assertEquals(5, ArrayListIterate.countWith(list, Predicates2.instanceOf(), Integer.class));
        Assert.assertEquals(0, ArrayListIterate.countWith(list, Predicates2.instanceOf(), Double.class));
    }

    @Test
    public void countWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        Assert.assertEquals(101, ArrayListIterate.countWith(list, Predicates2.instanceOf(), Integer.class));
        Assert.assertEquals(0, ArrayListIterate.countWith(list, Predicates2.instanceOf(), Double.class));
    }

    @Test
    public void collectIfOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        ArrayList<Class<?>> result = ArrayListIterate.collectIf(list, Predicates.equal(101), Functions.getToClass());
        Assert.assertEquals(FastList.newListWith(Integer.class), result);
    }

    @Test
    public void collectWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.oneTo(101));
        ArrayList<String> result = ArrayListIterate.collectWith(list, new Function2<Integer, Integer, String>()
        {
            public String value(Integer argument1, Integer argument2)
            {
                return argument1.equals(argument2) ? "101" : null;
            }
        }, 101);
        Verify.assertSize(101, result);
        Verify.assertContainsAll(result, null, "101");
        Assert.assertEquals(100, Iterate.count(result, Predicates.isNull()));
    }

    @Test
    public void detectIndexOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.toReverseList(1, 101));
        Assert.assertEquals(100, ArrayListIterate.detectIndex(list, Predicates.equal(1)));
        Assert.assertEquals(0, Iterate.detectIndex(list, Predicates.equal(101)));
        Assert.assertEquals(-1, Iterate.detectIndex(list, Predicates.equal(200)));
    }

    @Test
    public void detectIndexSmallList()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.toReverseList(1, 5));
        Assert.assertEquals(4, ArrayListIterate.detectIndex(list, Predicates.equal(1)));
        Assert.assertEquals(0, Iterate.detectIndex(list, Predicates.equal(5)));
        Assert.assertEquals(-1, Iterate.detectIndex(list, Predicates.equal(10)));
    }

    @Test
    public void detectIndexWithOver100()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.toReverseList(1, 101));
        Assert.assertEquals(100, Iterate.detectIndexWith(list, Predicates2.equal(), 1));
        Assert.assertEquals(0, Iterate.detectIndexWith(list, Predicates2.equal(), 101));
        Assert.assertEquals(-1, Iterate.detectIndexWith(list, Predicates2.equal(), 200));
    }

    @Test
    public void detectIndexWithSmallList()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.toReverseList(1, 5));
        Assert.assertEquals(4, Iterate.detectIndexWith(list, Predicates2.equal(), 1));
        Assert.assertEquals(0, Iterate.detectIndexWith(list, Predicates2.equal(), 5));
        Assert.assertEquals(-1, Iterate.detectIndexWith(list, Predicates2.equal(), 10));
    }

    @Test
    public void injectIntoWithOver100()
    {
        Sum result = new IntegerSum(0);
        Integer parameter = 2;
        Function3<Sum, Integer, Integer, Sum> function = new Function3<Sum, Integer, Integer, Sum>()
        {
            public Sum value(Sum sum, Integer element, Integer withValue)
            {
                return sum.add((element.intValue() - element.intValue()) * withValue.intValue());
            }
        };
        Sum sumOfDoubledValues = ArrayListIterate.injectIntoWith(result, this.getOver100IntegerList(), function, parameter);
        Assert.assertEquals(0, sumOfDoubledValues.getValue().intValue());
    }

    @Test
    public void removeIf()
    {
        ArrayList<Integer> objects = this.newArrayList(1, 2, 3, null);
        ArrayListIterate.removeIf(objects, Predicates.isNull());
        Verify.assertSize(3, objects);
        Verify.assertContainsAll(objects, 1, 2, 3);

        ArrayList<Integer> objects5 = this.newArrayList(null, 1, 2, 3);
        ArrayListIterate.removeIf(objects5, Predicates.isNull());
        Verify.assertSize(3, objects5);
        Verify.assertContainsAll(objects5, 1, 2, 3);

        ArrayList<Integer> objects4 = this.newArrayList(1, null, 2, 3);
        ArrayListIterate.removeIf(objects4, Predicates.isNull());
        Verify.assertSize(3, objects4);
        Verify.assertContainsAll(objects4, 1, 2, 3);

        ArrayList<Integer> objects3 = this.newArrayList(null, null, null, null);
        ArrayListIterate.removeIf(objects3, Predicates.isNull());
        Verify.assertEmpty(objects3);

        ArrayList<Integer> objects2 = this.newArrayList(null, 1, 2, 3, null);
        ArrayListIterate.removeIf(objects2, Predicates.isNull());
        Verify.assertSize(3, objects2);
        Verify.assertContainsAll(objects2, 1, 2, 3);

        ArrayList<Integer> objects1 = this.newArrayList(1, 2, 3);
        ArrayListIterate.removeIf(objects1, Predicates.isNull());
        Verify.assertSize(3, objects1);
        Verify.assertContainsAll(objects1, 1, 2, 3);

        ThisIsNotAnArrayList<Integer> objects6 = this.newNotAnArrayList(1, 2, 3);
        ArrayListIterate.removeIf(objects6, Predicates.isNull());
        Verify.assertSize(3, objects6);
        Verify.assertContainsAll(objects6, 1, 2, 3);
    }

    @Test
    public void removeIfWith()
    {
        ArrayList<Integer> objects = this.newArrayList(1, 2, 3, null);
        ArrayListIterate.removeIfWith(objects, Predicates2.isNull(), null);
        Verify.assertSize(3, objects);
        Verify.assertContainsAll(objects, 1, 2, 3);

        ArrayList<Integer> objects5 = this.newArrayList(null, 1, 2, 3);
        ArrayListIterate.removeIfWith(objects5, Predicates2.isNull(), null);
        Verify.assertSize(3, objects5);
        Verify.assertContainsAll(objects5, 1, 2, 3);

        ArrayList<Integer> objects4 = this.newArrayList(1, null, 2, 3);
        ArrayListIterate.removeIfWith(objects4, Predicates2.isNull(), null);
        Verify.assertSize(3, objects4);
        Verify.assertContainsAll(objects4, 1, 2, 3);

        ArrayList<Integer> objects3 = this.newArrayList(null, null, null, null);
        ArrayListIterate.removeIfWith(objects3, Predicates2.isNull(), null);
        Verify.assertEmpty(objects3);

        ArrayList<Integer> objects2 = this.newArrayList(null, 1, 2, 3, null);
        ArrayListIterate.removeIfWith(objects2, Predicates2.isNull(), null);
        Verify.assertSize(3, objects2);
        Verify.assertContainsAll(objects2, 1, 2, 3);

        ArrayList<Integer> objects1 = this.newArrayList(1, 2, 3);
        ArrayListIterate.removeIfWith(objects1, Predicates2.isNull(), null);
        Verify.assertSize(3, objects1);
        Verify.assertContainsAll(objects1, 1, 2, 3);

        ThisIsNotAnArrayList<Integer> objects6 = this.newNotAnArrayList(1, 2, 3);
        ArrayListIterate.removeIfWith(objects6, Predicates2.isNull(), null);
        Verify.assertSize(3, objects6);
        Verify.assertContainsAll(objects6, 1, 2, 3);
    }

    @Test
    public void take()
    {
        ArrayList<Integer> list = this.getIntegerList();

        Assert.assertEquals(FastList.newListWith(5, 4), ArrayListIterate.take(list, 2));

        Verify.assertSize(0, ArrayListIterate.take(list, 0));
        Verify.assertSize(5, ArrayListIterate.take(list, 5));

        Verify.assertSize(0, ArrayListIterate.take(new ArrayList<Integer>(), 2));

        ArrayList<Integer> list1 = new ArrayList<Integer>(130);
        list1.addAll(Interval.oneTo(120));
        Verify.assertListsEqual(Interval.oneTo(120), ArrayListIterate.take(list1, 125));
    }

    @Test(expected = IllegalArgumentException.class)
    public void take_throws()
    {
        ArrayListIterate.take(this.getIntegerList(), -1);
    }

    @Test
    public void drop()
    {
        ArrayList<Integer> list = this.getIntegerList();
        ArrayList<Integer> results = ArrayListIterate.drop(list, 2);
        Assert.assertEquals(FastList.newListWith(3, 2, 1), results);

        Verify.assertSize(0, ArrayListIterate.drop(list, 5));
        Verify.assertSize(0, ArrayListIterate.drop(list, 6));
        Verify.assertSize(5, ArrayListIterate.drop(list, 0));

        Verify.assertSize(0, ArrayListIterate.drop(new ArrayList<Integer>(), 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void drop_throws()
    {
        ArrayListIterate.drop(this.getIntegerList(), -1);
    }

    private ArrayList<Integer> newArrayList(Integer... items)
    {
        return new ArrayList<Integer>(mList(items));
    }

    private ThisIsNotAnArrayList<Integer> newNotAnArrayList(Integer... items)
    {
        return new ThisIsNotAnArrayList<Integer>(mList(items));
    }

    @Test
    public void groupByWithOptimisedList()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.toReverseList(1, 105));
        MutableMultimap<String, Integer> target = new FastListMultimap<String, Integer>();
        MutableMultimap<String, Integer> result = ArrayListIterate.groupBy(list, Functions.getToString(), target);
        Assert.assertEquals(result.get("105"), FastList.newListWith(105));
    }

    @Test
    public void groupByEachWithOptimisedList()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.toReverseList(1, 105));
        Function<Integer, Iterable<String>> function = new Function<Integer, Iterable<String>>()
        {
            public Iterable<String> valueOf(Integer object)
            {
                return FastList.newListWith(object.toString(), object.toString() + '*');
            }
        };
        MutableMultimap<String, Integer> target = new FastListMultimap<String, Integer>();
        MutableMultimap<String, Integer> result = ArrayListIterate.groupByEach(list, function, target);
        Assert.assertEquals(result.get("105"), FastList.newListWith(105));
        Assert.assertEquals(result.get("105*"), FastList.newListWith(105));
    }

    @Test
    public void flattenWithOptimisedArrays()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(Interval.toReverseList(1, 105));

        ArrayList<Integer> result = ArrayListIterate.flatCollect(list, new CollectionWrappingFunction<Integer>(),
                new ArrayList<Integer>());
        Assert.assertEquals(105, result.get(0).intValue());
    }

    private static class CollectionWrappingFunction<T> implements Function<T, Collection<T>>
    {
        private static final long serialVersionUID = 1L;

        public Collection<T> valueOf(T value)
        {
            return FastList.newListWith(value);
        }
    }
}
