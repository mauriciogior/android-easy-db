package com.mauriciogiordano.easydb;

import android.content.Context;
import android.test.ActivityUnitTestCase;

import com.mauriciogiordano.easydb.bean.Model;

import java.util.List;

public class ModelTestCase extends ActivityUnitTestCase<SampleActivity> {

    private Context context;

    public ModelTestCase() {
        super(SampleActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        context = getInstrumentation().getTargetContext();

       context.getSharedPreferences(Single.class.getPackage().getName()
                        + "." + Single.class.getName() + "." + "object",
                Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(Single.class.getPackage().getName()
                        + "." + Single.class.getName() + "." + "objectList",
                Context.MODE_PRIVATE).edit().clear().commit();

        context.getSharedPreferences(Father.class.getPackage().getName()
                        + "." + Father.class.getName() + "." + "object",
                Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(Father.class.getPackage().getName()
                        + "." + Father.class.getName() + "." + "objectList",
                Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(Father.class.getPackage().getName()
                        + "." + Father.class.getName() + "." + "children",
                Context.MODE_PRIVATE).edit().clear().commit();

        context.getSharedPreferences(Child.class.getPackage().getName()
                        + "." + Child.class.getName() + "." + "object",
                Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(Child.class.getPackage().getName()
                        + "." + Child.class.getName() + "." + "objectList",
                Context.MODE_PRIVATE).edit().clear().commit();

        super.setUp();
    }

    public void testSingle() {
        Single single = new Single(context);

        single.addOnUpdateListener(new Model.OnUpdateListener() {
            @Override
            public void onUpdate(Model object, Model.OnUpdateListener.Status status) {
                assertNotNull(object);
            }
        });

        single.setId("1");
        single.setHash(single.getClass().getName());
        single.save();

        Single lookForSingle = Single.find("1", context);

        assertNotNull("Object 1 should be found!", lookForSingle);
        assertTrue("Field should be equal!", lookForSingle.getId().equals(lookForSingle.getId()));
        assertTrue("Field should be equal!", lookForSingle.getHash().equals(lookForSingle.getHash()));

        assertTrue("Should be true!", lookForSingle.remove());

        lookForSingle = Single.find("1", context);

        assertNull("Should be null!", lookForSingle);

        single = new Single(context);

        single.setId("1");
        single.setHash(single.getClass().getName());
        single.save();

        single = new Single(context);

        single.setId("2");
        single.setHash(single.getClass().getName());
        single.save();

        List<Single> singleList = Single.findAll(context);

        assertEquals("Should have two objects!", 2, singleList.size());

        for(Single s : singleList) {
            assertEquals("Should be equal!", String.valueOf(singleList.indexOf(s) + 1), s.getId());
        }
    }

    public void testBroken() {
        try {
            new Broken(getInstrumentation().getTargetContext());
            fail("Should've thrown exception because of field 'HttpClient'.");
        } catch (RuntimeException e) {
            // success
        }
    }

    public void testOneToManyRelation() {
        Father father = new Father(getInstrumentation().getTargetContext());
        father.setId("1");
        father.setHash("Tom");
        father.save();

        Child fstChild = new Child(getInstrumentation().getTargetContext());
        fstChild.setId("100");
        fstChild.setHash("Nick");

        Child sndChild = new Child(getInstrumentation().getTargetContext());
        sndChild.setId("101");
        sndChild.setHash("Jonathan");

        father.addChild(fstChild);
        father.addChild(sndChild);

        assertEquals("Should be at position 0!", 0, father.indexOfChild(fstChild.getId()));
        assertEquals("Should be at position 1!", 1, father.indexOfChild(sndChild.getId()));
        assertEquals("Should be Nick!", "Nick", father.findChild("100").getHash());
        assertEquals("Should be Jonathan!", "Jonathan", father.findChild("101").getHash());

        List<Child> children = father.findAllChildren();

        assertEquals("Should be 2!", 2, children.size());

        for (Child aChildren : children) {
            assertEquals("Should be equal!", father.findChild(aChildren.getId()).getHash(), aChildren.getHash());
        }
    }

}
