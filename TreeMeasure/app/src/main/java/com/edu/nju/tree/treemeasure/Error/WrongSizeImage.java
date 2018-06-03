package com.edu.nju.tree.treemeasure.Error;

/**
 * Created by loick on 2018/6/3.
 */

public class WrongSizeImage extends RuntimeException {
    public WrongSizeImage() {
        super("图像大小应该为800*1200");
    }
}
