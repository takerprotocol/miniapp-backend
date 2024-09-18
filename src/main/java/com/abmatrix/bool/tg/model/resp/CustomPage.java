package com.abmatrix.bool.tg.model.resp;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义page对象
 * 
 * @param <T>
 * @author PeterWong
 * @date 2024年7月19日
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CustomPage<T> extends Page<T> {
	private static final long serialVersionUID = 1L;

	private Long pages;

	@Override
	public IPage<T> setPages(long pages) {
		this.pages = pages;
		return this;
	}

	@Override
	public long getPages() {
		return pages;
	}

}
