package com.fuint.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.fuint.common.service.BookService;
import com.fuint.common.service.StoreService;
import com.fuint.framework.annoation.OperationServiceLog;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationRequest;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.repository.mapper.MtBookMapper;
import com.fuint.repository.model.MtBanner;
import com.fuint.common.service.SettingService;
import com.fuint.common.enums.StatusEnum;
import com.fuint.repository.model.MtBook;
import com.fuint.repository.model.MtStore;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.pagehelper.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 预约服务接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Service
@AllArgsConstructor
public class BookServiceImpl extends ServiceImpl<MtBookMapper, MtBook> implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private MtBookMapper mtBookMapper;

    /**
     * 系统设置服务接口
     * */
    private SettingService settingService;

    /**
     * 店铺接口
     */
    private StoreService storeService;

    /**
     * 分页查询预约列表
     *
     * @param paginationRequest
     * @return
     */
    @Override
    public PaginationResponse<MtBook> queryBookListByPagination(PaginationRequest paginationRequest) {
        Page<MtBanner> pageHelper = PageHelper.startPage(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        LambdaQueryWrapper<MtBook> lambdaQueryWrapper = Wrappers.lambdaQuery();
        lambdaQueryWrapper.ne(MtBook::getStatus, StatusEnum.DISABLE.getKey());

        String name = paginationRequest.getSearchParams().get("name") == null ? "" : paginationRequest.getSearchParams().get("name").toString();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtBook::getName, name);
        }
        String status = paginationRequest.getSearchParams().get("status") == null ? "" : paginationRequest.getSearchParams().get("status").toString();
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtBook::getStatus, status);
        }
        String merchantId = paginationRequest.getSearchParams().get("merchantId") == null ? "" : paginationRequest.getSearchParams().get("merchantId").toString();
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtBook::getMerchantId, merchantId);
        }
        String storeId = paginationRequest.getSearchParams().get("storeId") == null ? "" : paginationRequest.getSearchParams().get("storeId").toString();
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.eq(MtBook::getStoreId, storeId);
        }

        lambdaQueryWrapper.orderByAsc(MtBook::getSort);
        List<MtBook> dataList = mtBookMapper.selectList(lambdaQueryWrapper);

        PageRequest pageRequest = PageRequest.of(paginationRequest.getCurrentPage(), paginationRequest.getPageSize());
        PageImpl pageImpl = new PageImpl(dataList, pageRequest, pageHelper.getTotal());
        PaginationResponse<MtBook> paginationResponse = new PaginationResponse(pageImpl, MtBook.class);
        paginationResponse.setTotalPages(pageHelper.getPages());
        paginationResponse.setTotalElements(pageHelper.getTotal());
        paginationResponse.setContent(dataList);

        return paginationResponse;
    }

    /**
     * 添加预约
     *
     * @param mtBook 预约信息
     * @return
     */
    @Override
    @OperationServiceLog(description = "添加预约")
    public MtBook addBook(MtBook mtBook) throws BusinessCheckException {
        Integer storeId = mtBook.getStoreId() == null ? 0 : mtBook.getStoreId();
        if (mtBook.getMerchantId() == null || mtBook.getMerchantId() <= 0) {
            MtStore mtStore = storeService.queryStoreById(storeId);
            if (mtStore != null) {
                mtBook.setMerchantId(mtStore.getMerchantId());
            }
        }
        if (mtBook.getMerchantId() == null || mtBook.getMerchantId() <= 0) {
            throw new BusinessCheckException("新增预约失败：所属商户不能为空！");
        }
        mtBook.setStoreId(storeId);
        mtBook.setStatus(StatusEnum.ENABLED.getKey());
        mtBook.setUpdateTime(new Date());
        mtBook.setCreateTime(new Date());
        Integer id = mtBookMapper.insert(mtBook);
        if (id > 0) {
            return mtBook;
        } else {
            logger.error("新增预约失败.");
            throw new BusinessCheckException("抱歉，新增预约失败！");
        }
    }

    /**
     * 根据ID获取预约信息
     *
     * @param id 预约ID
     * @return
     */
    @Override
    public MtBook getBookById(Integer id) {
        return mtBookMapper.selectById(id);
    }

    /**
     * 修改预约
     *
     * @param  mtBook
     * @throws BusinessCheckException
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @OperationServiceLog(description = "修改预约")
    public MtBook updateBook(MtBook mtBook) throws BusinessCheckException {
        MtBook book = getBookById(mtBook.getId());
        if (book == null) {
            throw new BusinessCheckException("该预约状态异常");
        }
        book.setId(book.getId());
        if (book.getLogo() != null) {
            book.setLogo(mtBook.getLogo());
        }
        if (book.getName() != null) {
            book.setName(mtBook.getName());
        }
        if (mtBook.getStoreId() != null) {
            book.setStoreId(mtBook.getStoreId());
        }
        if (mtBook.getDescription() != null) {
            book.setDescription(mtBook.getDescription());
        }
        if (mtBook.getOperator() != null) {
            book.setOperator(mtBook.getOperator());
        }
        if (mtBook.getStatus() != null) {
            book.setStatus(mtBook.getStatus());
        }
        if (mtBook.getGoodsId() != null) {
            book.setGoodsId(mtBook.getGoodsId());
        }
        if (mtBook.getSort() != null) {
            book.setSort(mtBook.getSort());
        }
        if (mtBook.getServiceDates() != null) {
            book.setServiceDates(mtBook.getServiceDates());
        }
        if (mtBook.getServiceTimes() != null) {
            book.setServiceTimes(mtBook.getServiceTimes());
        }
        if (mtBook.getServiceStaffIds() != null) {
            book.setServiceStaffIds(mtBook.getServiceStaffIds());
        }
        book.setUpdateTime(new Date());
        mtBookMapper.updateById(book);
        return book;
    }

    /**
     * 根据条件搜索预约
     *
     * @param  params 查询参数
     * @throws BusinessCheckException
     * @return
     * */
    @Override
    public List<MtBook> queryBookListByParams(Map<String, Object> params) {
        String status =  params.get("status") == null ? StatusEnum.ENABLED.getKey(): params.get("status").toString();
        String storeId =  params.get("storeId") == null ? "" : params.get("storeId").toString();
        String merchantId =  params.get("merchantId") == null ? "" : params.get("merchantId").toString();
        String name = params.get("name") == null ? "" : params.get("name").toString();

        LambdaQueryWrapper<MtBook> lambdaQueryWrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(name)) {
            lambdaQueryWrapper.like(MtBook::getName, name);
        }
        if (StringUtils.isNotBlank(status)) {
            lambdaQueryWrapper.eq(MtBook::getStatus, status);
        }
        if (StringUtils.isNotBlank(merchantId)) {
            lambdaQueryWrapper.eq(MtBook::getMerchantId, merchantId);
        }
        if (StringUtils.isNotBlank(storeId)) {
            lambdaQueryWrapper.eq(MtBook::getStoreId, storeId);
        }

        lambdaQueryWrapper.orderByAsc(MtBook::getSort);
        List<MtBook> dataList = mtBookMapper.selectList(lambdaQueryWrapper);
        String baseImage = settingService.getUploadBasePath();

        if (dataList.size() > 0) {
            for (MtBook book : dataList) {
                 book.setLogo(baseImage + book.getLogo());
            }
        }

        return dataList;
    }
}
