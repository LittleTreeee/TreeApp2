package com.edu.nju.tree.treemeasure.Error;

/**
 * Created by loick on 2018/5/30.
 */

public class NoRedPointsException extends RuntimeException {

    public NoRedPointsException() {
        super("指定区域内没有红色光点");
    }

}
