package com.imgix.test;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import com.imgix.URLBuilder;

import java.security.NoSuchAlgorithmException;
import java.util.*;

@RunWith(JUnit4.class)
public class TestSrcSet {

    private static HashMap<String, String> params;
    private static String[] srcsetSplit;
    private static String[] srcsetWidthSplit;
    private static String[] srcsetHeightSplit;
    private static String[] srcsetAspectRatioSplit;
    private static String[] srcsetWidthAndHeightSplit;
    private static String[] srcsetWidthAndAspectRatioSplit;
    private static String[] srcsetHeightAndAspectRatioSplit;

    @BeforeClass
    public static void buildAllSrcSets() {
        String srcset, srcsetWidth, srcsetHeight, srcsetAspectRatio, srcsetWidthAndHeight, srcsetWidthAndAspectRatio, srcsetHeightAndAspectRatio;

        URLBuilder ub = new URLBuilder("test.imgix.net", true, "MYT0KEN" , false);
        params = new HashMap<String, String>();

        srcset = ub.createSrcSet("image.jpg");
        srcsetSplit = srcset.split(",");

        params.put("w","300");
        srcsetWidth = ub.createSrcSet("image.jpg", params);
        srcsetWidthSplit = srcsetWidth.split(",");
        params.clear();

        params.put("h","300");
        srcsetHeight = ub.createSrcSet("image.jpg", params);
        srcsetHeightSplit = srcsetHeight.split(",");
        params.clear();

        params.put("ar","3:2");
        srcsetAspectRatio = ub.createSrcSet("image.jpg", params);
        srcsetAspectRatioSplit = srcsetAspectRatio.split(",");
        params.clear();

        params.put("w","300");
        params.put("h","300");
        srcsetWidthAndHeight = ub.createSrcSet("image.jpg", params);
        srcsetWidthAndHeightSplit = srcsetWidthAndHeight.split(",");
        params.clear();

        params.put("w","300");
        params.put("ar","3:2");
        srcsetWidthAndAspectRatio = ub.createSrcSet("image.jpg", params);
        srcsetWidthAndAspectRatioSplit = srcsetWidthAndAspectRatio.split(",");
        params.clear();

        params.put("h","300");
        params.put("ar","3:2");
        srcsetHeightAndAspectRatio = ub.createSrcSet("image.jpg", params);
        srcsetHeightAndAspectRatioSplit = srcsetHeightAndAspectRatio.split(",");
        params.clear();
    }

    @Test
    public void testNoParametersGeneratesCorrectWidths() {
        int[] targetWidths = {100, 116, 134, 156, 182, 210, 244, 282,
                328, 380, 442, 512, 594, 688, 798, 926,
                1074, 1246, 1446, 1678, 1946, 2258, 2618,
                3038, 3524, 4088, 4742, 5500, 6380, 7400, 8192};

        String generatedWidth;
        int index = 0;
        int widthInt;

        for (String src: srcsetSplit) {
            generatedWidth = src.split(" ")[1];
            widthInt = Integer.parseInt(generatedWidth.substring(0,generatedWidth.length()-1));
            assertEquals(targetWidths[index], widthInt);
            index++;
        }
    }

    @Test
    public void testNoParametersReturnsExpectedNumberOfPairs() {
        int expectedPairs = 31;
        assertEquals(expectedPairs, srcsetSplit.length);
    }

    @Test
    public void testNoParametersDoesNotExceedBounds() {
        String minWidth = srcsetSplit[0].split(" ")[1];
        String maxWidth = srcsetSplit[srcsetSplit.length-1].split(" ")[1];

        int minWidthInt = Integer.parseInt(minWidth.substring(0,minWidth.length()-1));
        int maxWidthInt = Integer.parseInt(maxWidth.substring(0,maxWidth.length()-1));

        assert(minWidthInt >= 100);
        assert(maxWidthInt <= 8192);
    }

    // a 17% testing threshold is used to account for rounding
    @Test
    public void testNoParametersDoesNotIncreaseMoreThan17Percent() {
        final double INCREMENT_ALLOWED = .17;
        String width;
        int widthInt, prev;

        // convert and store first width (typically: 100)
        width = srcsetSplit[0].split(" ")[1];
        prev = Integer.parseInt(width.substring(0,width.length()-1));

        for (String src : srcsetSplit) {
            width = src.split(" ")[1];
            widthInt = Integer.parseInt(width.substring(0,width.length()-1));

            assert((widthInt / prev) < (1 + INCREMENT_ALLOWED));
            prev = widthInt;
        }
    }

    @Test
    public void testNoParametersSignsUrls() {
        String src, parameters, generatedSignature, expectedSignature = "", signatureBase;

        for (String srcLine : srcsetSplit) {

            src = srcLine.split(" ")[0];
            assert(src.contains("s="));
            generatedSignature = src.substring(src.indexOf("s=")+2);

            parameters = src.substring(src.indexOf("?"), src.indexOf("s=")-1);
            signatureBase = "MYT0KEN" + "/image.jpg" + parameters;

            // create MD5 hash
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] array = md.digest(signatureBase.getBytes("UTF-8"));
                StringBuffer sb = new StringBuffer();
                for (int x = 0; x < array.length; ++x) {
                    sb.append(Integer.toHexString((array[x] & 0xFF) | 0x100).substring(1,3));
                }
                expectedSignature = sb.toString();
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            }

            assertEquals(expectedSignature, generatedSignature);
        }
    }

    @Test
    public void testWidthInDPRForm() {
        String generatedRatio;
        int expectedRatio = 1;
        assert(srcsetWidthSplit.length == 5);

        for (String src: srcsetWidthSplit) {
            generatedRatio = src.split(" ")[1];
            assertEquals(expectedRatio + "x", generatedRatio);
            expectedRatio++;
        }
    }

    @Test
    public void testWidthSignsUrls() {
        String src, parameters, generatedSignature, expectedSignature = "", signatureBase;

        for (String srcLine : srcsetWidthSplit) {

            src = srcLine.split(" ")[0];
            assert(src.contains("s="));
            generatedSignature = src.substring(src.indexOf("s=")+2);

            parameters = src.substring(src.indexOf("?"), src.indexOf("s=")-1);
            signatureBase = "MYT0KEN" + "/image.jpg" + parameters;

            // create MD5 hash
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] array = md.digest(signatureBase.getBytes("UTF-8"));
                StringBuffer sb = new StringBuffer();
                for (int x = 0; x < array.length; ++x) {
                    sb.append(Integer.toHexString((array[x] & 0xFF) | 0x100).substring(1,3));
                }
                expectedSignature = sb.toString();
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            }

            assertEquals(expectedSignature, generatedSignature);
        }
    }

    @Test
    public void testWidthIncludesDPRParam() {
        String src;

        for (int i = 0; i < srcsetWidthSplit.length; i++) {
            src = srcsetWidthSplit[i].split(" ")[0];
            assert(src.contains(String.format("dpr=%s", i+1)));
        }
    }

    @Test
    public void testHeightGeneratesCorrectWidths() {
        int[] targetWidths = {100, 116, 134, 156, 182, 210, 244, 282,
                328, 380, 442, 512, 594, 688, 798, 926,
                1074, 1246, 1446, 1678, 1946, 2258, 2618,
                3038, 3524, 4088, 4742, 5500, 6380, 7400, 8192};

        String generatedWidth;
        int index = 0;
        int widthInt;

        for (String src: srcsetHeightSplit) {
            generatedWidth = src.split(" ")[1];
            widthInt = Integer.parseInt(generatedWidth.substring(0,generatedWidth.length()-1));
            assertEquals(targetWidths[index], widthInt);
            index++;
        }
    }

    @Test
    public void testHeightContainsHeightParameter() {
        String url;

        for (String src: srcsetHeightSplit) {
            url = src.split(" ")[0];
            assert(url.contains("h="));
        }
    }

    @Test
    public void testHeightReturnsExpectedNumberOfPairs() {
        int expectedPairs = 31;
        assertEquals(expectedPairs, srcsetHeightSplit.length);
    }

    @Test
    public void testHeightDoesNotExceedBounds() {
        String minWidth = srcsetHeightSplit[0].split(" ")[1];
        String maxWidth = srcsetHeightSplit[srcsetHeightSplit.length-1].split(" ")[1];

        int minWidthInt = Integer.parseInt(minWidth.substring(0,minWidth.length()-1));
        int maxWidthInt = Integer.parseInt(maxWidth.substring(0,maxWidth.length()-1));

        assert(minWidthInt >= 100);
        assert(maxWidthInt <= 8192);
    }

    // a 17% testing threshold is used to account for rounding
    @Test
    public void testHeightDoesNotIncreaseMoreThan17Percent() {
        final double INCREMENT_ALLOWED = .17;
        String width;
        int widthInt, prev;

        // convert and store first width (typically: 100)
        width = srcsetHeightSplit[0].split(" ")[1];
        prev = Integer.parseInt(width.substring(0,width.length()-1));

        for (String src : srcsetHeightSplit) {
            width = src.split(" ")[1];
            widthInt = Integer.parseInt(width.substring(0,width.length()-1));

            assert((widthInt / prev) < (1 + INCREMENT_ALLOWED));
            prev = widthInt;
        }
    }

    @Test
    public void testHeightSignsUrls() {
        String src, parameters, generatedSignature, expectedSignature = "", signatureBase;

        for (String srcLine : srcsetHeightSplit) {

            src = srcLine.split(" ")[0];
            assert(src.contains("s="));
            generatedSignature = src.substring(src.indexOf("s=")+2);

            parameters = src.substring(src.indexOf("?"), src.indexOf("s=")-1);
            signatureBase = "MYT0KEN" + "/image.jpg" + parameters;

            // create MD5 hash
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] array = md.digest(signatureBase.getBytes("UTF-8"));
                StringBuffer sb = new StringBuffer();
                for (int x = 0; x < array.length; ++x) {
                    sb.append(Integer.toHexString((array[x] & 0xFF) | 0x100).substring(1,3));
                }
                expectedSignature = sb.toString();
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            }

            assertEquals(expectedSignature, generatedSignature);
        }
    }

    @Test
    public void testWidthAndHeightInDPRForm() {
        String generatedRatio;
        int expectedRatio = 1;
        assert(srcsetWidthAndHeightSplit.length == 5);

        for (String src: srcsetWidthAndHeightSplit) {
            generatedRatio = src.split(" ")[1];
            assertEquals(expectedRatio + "x", generatedRatio);
            expectedRatio++;
        }
    }

    @Test
    public void testWidthAndHeightSignsUrls() {
        String src, parameters, generatedSignature, expectedSignature = "", signatureBase;

        for (String srcLine : srcsetWidthAndHeightSplit) {

            src = srcLine.split(" ")[0];
            assert(src.contains("s="));
            generatedSignature = src.substring(src.indexOf("s=")+2);

            parameters = src.substring(src.indexOf("?"), src.indexOf("s=")-1);
            signatureBase = "MYT0KEN" + "/image.jpg" + parameters;

            // create MD5 hash
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] array = md.digest(signatureBase.getBytes("UTF-8"));
                StringBuffer sb = new StringBuffer();
                for (int x = 0; x < array.length; ++x) {
                    sb.append(Integer.toHexString((array[x] & 0xFF) | 0x100).substring(1,3));
                }
                expectedSignature = sb.toString();
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            }

            assertEquals(expectedSignature, generatedSignature);
        }
    }

    @Test
    public void testWidthAndHeightIncludesDPRParam() {
        String src;

        for (int i = 0; i < srcsetWidthAndHeightSplit.length; i++) {
            src = srcsetWidthAndHeightSplit[i].split(" ")[0];
            assert(src.contains(String.format("dpr=%s", i+1)));
        }
    }

    @Test
    public void testAspectRatioGeneratesCorrectWidths() {
        int[] targetWidths = {100, 116, 134, 156, 182, 210, 244, 282,
                328, 380, 442, 512, 594, 688, 798, 926,
                1074, 1246, 1446, 1678, 1946, 2258, 2618,
                3038, 3524, 4088, 4742, 5500, 6380, 7400, 8192};

        String generatedWidth;
        int index = 0;
        int widthInt;

        for (String src: srcsetAspectRatioSplit) {
            generatedWidth = src.split(" ")[1];
            widthInt = Integer.parseInt(generatedWidth.substring(0,generatedWidth.length()-1));
            assertEquals(targetWidths[index], widthInt);
            index++;
        }
    }

    @Test
    public void testAspectRatioContainsARParameter() {
        String url;

        for (String src: srcsetAspectRatioSplit) {
            url = src.split(" ")[0];
            assert(url.contains("ar="));
        }
    }

    @Test
    public void testAspectRatioReturnsExpectedNumberOfPairs() {
        int expectedPairs = 31;
        assertEquals(expectedPairs, srcsetAspectRatioSplit.length);
    }

    @Test
    public void testAspectRatioDoesNotExceedBounds() {
        String minWidth = srcsetAspectRatioSplit[0].split(" ")[1];
        String maxWidth = srcsetAspectRatioSplit[srcsetAspectRatioSplit.length-1].split(" ")[1];

        int minWidthInt = Integer.parseInt(minWidth.substring(0,minWidth.length()-1));
        int maxWidthInt = Integer.parseInt(maxWidth.substring(0,maxWidth.length()-1));

        assert(minWidthInt >= 100);
        assert(maxWidthInt <= 8192);
    }

    // a 17% testing threshold is used to account for rounding
    @Test
    public void testAspectRatioDoesNotIncreaseMoreThan17Percent() {
        final double INCREMENT_ALLOWED = .17;
        String width;
        int widthInt, prev;

        // convert and store first width (typically: 100)
        width = srcsetAspectRatioSplit[0].split(" ")[1];
        prev = Integer.parseInt(width.substring(0,width.length()-1));

        for (String src : srcsetAspectRatioSplit) {
            width = src.split(" ")[1];
            widthInt = Integer.parseInt(width.substring(0,width.length()-1));

            assert((widthInt / prev) < (1 + INCREMENT_ALLOWED));
            prev = widthInt;
        }
    }

    @Test
    public void testAspectRatioSignsUrls() {
        String src, parameters, generatedSignature, expectedSignature = "", signatureBase;

        for (String srcLine : srcsetAspectRatioSplit) {

            src = srcLine.split(" ")[0];
            assert(src.contains("s="));
            generatedSignature = src.substring(src.indexOf("s=")+2);

            parameters = src.substring(src.indexOf("?"), src.indexOf("s=")-1);
            signatureBase = "MYT0KEN" + "/image.jpg" + parameters;

            // create MD5 hash
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] array = md.digest(signatureBase.getBytes("UTF-8"));
                StringBuffer sb = new StringBuffer();
                for (int x = 0; x < array.length; ++x) {
                    sb.append(Integer.toHexString((array[x] & 0xFF) | 0x100).substring(1,3));
                }
                expectedSignature = sb.toString();
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            }

            assertEquals(expectedSignature, generatedSignature);
        }
    }

    @Test
    public void testWidthAndAspectRatioInDPRForm() {
        String generatedRatio;
        int expectedRatio = 1;
        assert(srcsetWidthAndAspectRatioSplit.length == 5);

        for (String src: srcsetWidthAndAspectRatioSplit) {
            generatedRatio = src.split(" ")[1];
            assertEquals(expectedRatio + "x", generatedRatio);
            expectedRatio++;
        }
    }

    @Test
    public void testWidthAndAspectRatioSignsUrls() {
        String src, parameters, generatedSignature, expectedSignature = "", signatureBase;

        for (String srcLine : srcsetWidthAndAspectRatioSplit) {

            src = srcLine.split(" ")[0];
            assert(src.contains("s="));
            generatedSignature = src.substring(src.indexOf("s=")+2);

            parameters = src.substring(src.indexOf("?"), src.indexOf("s=")-1);
            signatureBase = "MYT0KEN" + "/image.jpg" + parameters;

            // create MD5 hash
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] array = md.digest(signatureBase.getBytes("UTF-8"));
                StringBuffer sb = new StringBuffer();
                for (int x = 0; x < array.length; ++x) {
                    sb.append(Integer.toHexString((array[x] & 0xFF) | 0x100).substring(1,3));
                }
                expectedSignature = sb.toString();
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            }

            assertEquals(expectedSignature, generatedSignature);
        }
    }

    @Test
    public void testWidthAndAspectRatioIncludesDPRParam() {
        String src;

        for (int i = 0; i < srcsetWidthAndAspectRatioSplit.length; i++) {
            src = srcsetWidthAndAspectRatioSplit[i].split(" ")[0];
            assert(src.contains(String.format("dpr=%s", i+1)));
        }
    }

    @Test
    public void testHeightAndAspectRatioInDPRForm() {
        String generatedRatio;
        int expectedRatio = 1;
        assert(srcsetHeightAndAspectRatioSplit.length == 5);

        for (String src: srcsetHeightAndAspectRatioSplit) {
            generatedRatio = src.split(" ")[1];
            assertEquals(expectedRatio + "x", generatedRatio);
            expectedRatio++;
        }
    }

    @Test
    public void testHeightAndAspectRatioSignsUrls() {
        String src, parameters, generatedSignature, expectedSignature = "", signatureBase;

        for (String srcLine : srcsetHeightAndAspectRatioSplit) {

            src = srcLine.split(" ")[0];
            assert(src.contains("s="));
            generatedSignature = src.substring(src.indexOf("s=")+2);

            parameters = src.substring(src.indexOf("?"), src.indexOf("s=")-1);
            signatureBase = "MYT0KEN" + "/image.jpg" + parameters;

            // create MD5 hash
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] array = md.digest(signatureBase.getBytes("UTF-8"));
                StringBuffer sb = new StringBuffer();
                for (int x = 0; x < array.length; ++x) {
                    sb.append(Integer.toHexString((array[x] & 0xFF) | 0x100).substring(1,3));
                }
                expectedSignature = sb.toString();
            } catch (UnsupportedEncodingException e) {
            } catch (NoSuchAlgorithmException e) {
            }

            assertEquals(expectedSignature, generatedSignature);
        }
    }

    @Test
    public void testHeightAndAspectRatioIncludesDPRParam() {
        String src;

        for (int i = 0; i < srcsetHeightAndAspectRatioSplit.length; i++) {
            src = srcsetHeightAndAspectRatioSplit[i].split(" ")[0];
            assert(src.contains(String.format("dpr=%s", i+1)));
        }
    }
}
