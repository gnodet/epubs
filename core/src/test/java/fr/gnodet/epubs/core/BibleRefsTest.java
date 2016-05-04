package fr.gnodet.epubs.core;

import org.junit.Assert;
import org.junit.Test;

public class BibleRefsTest {

    @Test
    public void testBibleRefs1() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Jésus-Christ lui-même (cf. Hb v, 10 ; vi,20 ; vii,1.10.11.15).</p>");
        Assert.assertEquals("<p>Jésus-Christ lui-même (cf. <bible><ss>Hb</ss>\u00a0<sc>5</sc>, <sv>10</sv>\u00a0; <sc>6</sc>, <sv>20</sv> ; <sc>7</sc>, <sv>1</sv>.<sv>10</sv>.<sv>11</sv>.<sv>15</sv></bible>).</p>", fixed);
    }

    @Test
    public void testBibleRefs2() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Matth., XXIV, 6, 7.</p><p>Luc., II, 14.</p>");
        Assert.assertEquals("<p><bible><ss>Mt</ss>\u00a0<sc>24</sc>, <sv>6</sv>, <sv>7</sv></bible>.</p><p><bible><ss>Lc</ss>\u00a0<sc>2</sc>, <sv>14</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs3() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Ioan., XIII, 34.</p>");
        Assert.assertEquals("<p><bible><ss>Jn</ss>\u00a0<sc>13</sc>, <sv>34</sv></bible>.</p>", fixed);

        fixed = BibleRefs.fixBibleRefs("<p>Id., XV, 12.</p>");
        Assert.assertEquals("<p><bible><ss>Jn</ss>\u00a0<sc>15</sc>, <sv>12</sv></bible>.</p>", fixed);

        fixed = BibleRefs.fixBibleRefs("<p>Id., ibid., 17.</p>");
        Assert.assertEquals("<p><bible><ss>Jn</ss>\u00a0<sc>15</sc>, <sv>17</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs4() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>ISAÏE XXXII, 17.</p>");
        Assert.assertEquals("<p><bible><ss>Is</ss>\u00a0<sc>32</sc>, <sv>17</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs5() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>S. JEAN XIV, 14.</p>");
        Assert.assertEquals("<p><bible><ss>Jn</ss>\u00a0<sc>14</sc>, <sv>14</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs6() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>III JEAN 14.</p>");
        Assert.assertEquals("<p><bible><ss>3Jn</ss>\u00a0<sv>14</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs7() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Jean XVII, 8 et 14.</p>");
        Assert.assertEquals("<p><bible><ss>Jn</ss>\u00a0<sc>17</sc>, <sv>8</sv> et <sv>14</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs8() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Ep 14.</p>");
        Assert.assertEquals("<p>Ep 14.</p>", fixed);
    }

    @Test
    public void testBibleRefs9() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>(Ac 20, 35)</p>");
        Assert.assertEquals("<p>(<bible><ss>Ac</ss>\u00a0<sc>20</sc>, <sv>35</sv></bible>)</p>", fixed);
    }

    @Test
    public void testBibleRefs10() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Ps 54 (53), 12</p>");
        Assert.assertEquals("<p><bible><ss>Ps</ss> <sc>54</sc> (53), <sv>12</sv></bible></p>", fixed);
    }

    @Test
    public void testBibleRefs11() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>1 Thess. 2, 11.</p>");
        Assert.assertEquals("<p><bible><ss>1Th</ss> <sc>2</sc>, <sv>11</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs12() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>1 Thess. 2, 11 ; 1Co 4, 15.</p>");
        Assert.assertEquals("<p><bible><ss>1Th</ss> <sc>2</sc>, <sv>11</sv></bible> ; <bible><ss>1Co</ss> <sc>4</sc>, <sv>15</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs13() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Cf. 1 Thess. 2, 11 ; 1Co 4, 15.</p>");
        Assert.assertEquals("<p>Cf. <bible><ss>1Th</ss> <sc>2</sc>, <sv>11</sv></bible> ; <bible><ss>1Co</ss> <sc>4</sc>, <sv>15</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs14() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p><i>Eph</i> 5, 27</p>");
        Assert.assertEquals("<p><bible><ss>Ep</ss> <sc>5</sc>, <sv>27</sv></bible></p>", fixed);
    }

    @Test
    public void testBibleRefs15() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>1P 2, 9</p>");
        Assert.assertEquals("<p><bible><ss>1P</ss> <sc>2</sc>, <sv>9</sv></bible></p>", fixed);
    }

    @Test
    public void testBibleRefs16() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>3 R 19, 8</p>");
        Assert.assertEquals("<p><bible><ss>3R</ss> <sc>19</sc>, <sv>8</sv></bible></p>", fixed);
    }

    @Test
    public void testBibleRefs17() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Ps 145(144), 9</p>");
        Assert.assertEquals("<p><bible><ss>Ps</ss> <sc>145</sc>(144), <sv>9</sv></bible></p>", fixed);
    }

    @Test
    public void testBibleRefs18() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Cf. Is 35, 5 ; 61, 1-3.</p>");
        Assert.assertEquals("<p>Cf. <bible><ss>Is</ss> <sc>35</sc>, <sv>5</sv> ; <sc>61</sc>, <sv>1</sv>-<sv>3</sv></bible>.</p>", fixed);
    }

    @Test
    public void testBibleRefs19() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>Ps 85(84) : 11</p>");
        Assert.assertEquals("<p><bible><ss>Ps</ss> <sc>85</sc>(84), <sv>11</sv></bible></p>", fixed);
    }

    @Test
    public void testBibleRefs20() throws Exception {
        String fixed = BibleRefs.fixBibleRefs("<p>He 1, 1s.</p>");
        Assert.assertEquals("<p><bible><ss>Hb</ss> <sc>1</sc>, <sv>1</sv> s.</bible></p>", fixed);
    }

}
