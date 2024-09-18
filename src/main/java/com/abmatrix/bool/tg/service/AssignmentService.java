package com.abmatrix.bool.tg.service;

import com.abmatrix.bool.tg.common.model.vo.ResultVo;
import com.abmatrix.bool.tg.model.req.AssignmentReq;
import com.abmatrix.bool.tg.model.resp.AssignmentResp;

import java.util.List;

/**
 * 小任务接口
 * 
 * @author PeterWong
 * @date 2024年8月3日
 */
public interface AssignmentService {
	/**
	 * 查询任务列表
	 * 
	 * @param req
	 * @return
	 */
	List<AssignmentResp> queryAssignments(AssignmentReq req);

	/**
	 * 完成任务
	 * 
	 * @param req
	 */
	ResultVo<Boolean> completeAssignment(AssignmentReq req);
}
