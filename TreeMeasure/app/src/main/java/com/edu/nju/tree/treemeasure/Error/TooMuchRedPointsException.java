package com.edu.nju.tree.treemeasure.Error;

/**
 * Created by loick on 2018/5/30.
 */

public class TooMuchRedPointsException extends RuntimeException {

    public TooMuchRedPointsException() {
        super("指定区域内红色光点多余两个，有其他红色干扰物");
    }
}
