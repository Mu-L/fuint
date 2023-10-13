package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuint.common.dto.MemberGroupDto;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.*;
import com.fuint.common.util.CommonUtil;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.mapper.MtUserGroupMapper;
import com.fuint.repository.model.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.lang.String;
import java.util.*;

/**
 * 会员分组业务实现类
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
public class MemberGroupServiceImpl extends ServiceImpl<MtUserGroupMapper, MtUserGroup> implements MemberGroupService {

    private static final Logger log = LoggerFactory.getLogger(CouponGroupServiceImpl.class);

    @Resource
    private MtUserGroupMapper mtUserGroupMapper;

    /**
     * 分页查询会员分组列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<MtUserGroup> queryMemberGroupListByPagination(PaginationRequest paginationRequest) {
        Page<MtCouponGroup> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtUserGroup> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtUserGroup::getStatus, StatusEnum.DISABLE.getKey());

        String name = paginationRequest.getSearchParams().get("name") == null ? "" : paginationRequest.getSearchParams().get("name").toString();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtUserGroup::getName, name);
        }
        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtUserGroup::getStatus, status);
        }
        String id = paginationRequest.getSearchParams().get("id") == null ? "" : paginationRequest.getSearchParams().get("id").toString();
        if (StringUtils.isNotBlank(id)) {
            lambdaQueryWrapper.eq(MtUserGroup::getId, id);
        }
        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtUserGroup::getMerchantId, merchantId);
        }
        String storeId = paginationRequest.getSearchParams().get("storeId") == null ? "" : paginationRequest.getSearchParams().get("storeId").toString();
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.eq(MtUserGroup::getStoreId, storeId);
        }

        lambdaQueryWrapper.orderByDesc(MtUserGroup::getId);
        List<MtUserGroup> dataList = mtUserGroupMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtUserGroup> paginationResponse = new PaginationResponse(pageImpl, MtUserGroup.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 添加会员分组
     *
     * @param  memberGroupDto
     * @throws BusinessCheckException
     */
    @Override
    @OperationServiceLog(description = "新增会员分组")
    public MtUserGroup addMemberGroup(MemberGroupDto memberGroupDto) {
        MtUserGroup userGroup = new MtUserGroup();
        userGroup.setMerchantId(memberGroupDto.getMerchantId());
        userGroup.setStoreId(memberGroupDto.getStoreId());
        userGroup.setName(CommonUtil.replaceXSS(memberGroupDto.getName()));
        userGroup.setDescription(CommonUtil.replaceXSS(memberGroupDto.getDescription()));
        userGroup.setStatus(StatusEnum.ENABLED.getKey());
        userGroup.setCreateTime(new Date());
        userGroup.setUpdateTime(new Date());
        userGroup.setOperator(memberGroupDto.getOperator());
        this.save(userGroup);
        return userGroup;
    }

    /**
     * 根据分组ID获取分组信息
     *
     * @param  id 分组ID
     * @throws BusinessCheckException
     */
    @Override
    public MtUserGroup queryMemberGroupById(Integer id) {
        return mtUserGroupMapper.selectById(id);
    }

    /**
     * 根据ID删除会员分组
     *
     * @param  id       分组ID
     * @param  operator 操作人
     * @throws BusinessCheckException
     */
    @Override
    @OperationServiceLog(description = "删除会员分组")
    public void deleteMemberGroup(Integer id, String operator) {
        MtUserGroup userGroup = queryMemberGroupById(id);
        if (null == userGroup) {
            return;
        }

        userGroup.setStatus(StatusEnum.DISABLE.getKey());
        userGroup.setUpdateTime(new Date());
        userGroup.setOperator(operator);

        this.updateById(userGroup);
    }

    /**
     * 修改卡券分组
     *
     * @param  memberGroupDto
     * @throws BusinessCheckException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "更新会员分组")
    public MtUserGroup updateMemberGroup(MemberGroupDto memberGroupDto) throws BusinessCheckException {
        MtUserGroup userGroup = queryMemberGroupById(memberGroupDto.getId());
        if (null == userGroup || StatusEnum.DISABLE.getKey().equalsIgnoreCase(userGroup.getStatus())) {
            log.error("该分组不存在或已被删除");
            throw new BusinessCheckException("该分组不存在或已被删除");
        }
        if (memberGroupDto.getName() != null) {
            userGroup.setName(CommonUtil.replaceXSS(memberGroupDto.getName()));
        }
        if (memberGroupDto.getDescription() != null) {
            userGroup.setDescription(CommonUtil.replaceXSS(memberGroupDto.getDescription()));
        }
        if (memberGroupDto.getStatus() != null) {
            userGroup.setStatus(memberGroupDto.getStatus());
        }

        userGroup.setUpdateTime(new Date());
        userGroup.setOperator(memberGroupDto.getOperator());
        this.updateById(userGroup);
        return userGroup;
    }
}
