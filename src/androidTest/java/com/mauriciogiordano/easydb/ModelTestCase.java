package com.mauriciogiordano.easydb;

import android.test.ActivityUnitTestCase;

import com.mauriciogiordano.easydb.bean.Model;

import java.util.List;

public class ModelTestCase extends ActivityUnitTestCase<SampleActivity> {

    public ModelTestCase() {
        super(SampleActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSingle() {
        Single fstSingle = new Single(getInstrumentation().getTargetContext());
        fstSingle.addOnUpdateListener(new Model.OnUpdateListener() {
            @Override
            public void onUpdate(Model object, Model.OnUpdateListener.Status status) {
                assertNotNull(object);
            }
        });

        fstSingle.setId("1");
        fstSingle.setHash(fstSingle.getClass().getName());
        fstSingle.save();

        Single sndSingle = fstSingle.find("1");

        assertNotNull("Object 1 should be found!", sndSingle);
        assertTrue("Field should be equal!", sndSingle.getId().equals(fstSingle.getId()));
        assertTrue("Field should be equal!", sndSingle.getHash().equals(fstSingle.getHash()));

        sndSingle.remove();

        sndSingle = fstSingle.find("1");

        assertNull("Should be null!", sndSingle);
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
