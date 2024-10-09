package com.dkd.manage.service.impl;

import java.util.List;
import com.dkd.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.dkd.manage.mapper.VendingMachineMapper;
import com.dkd.manage.domain.VendingMachine;
import com.dkd.manage.service.IVendingMachineService;

/**
 * 设备管理Service业务层处理
 * 
 * @author huang
 * @date 2024-06-21
 */
@Service
public class VendingMachineServiceImpl implements IVendingMachineService 
{
    @Autowired
    private VendingMachineMapper vendingMachineMapper;
    @Autowired
    private INodeService nodeService;
    @Autowired
    private IVmTypeService vmTypeService;
    @Autowired
    private IChannelService channelService;

    /**
     * 查询设备管理
     * 
     * @param id 设备管理主键
     * @return 设备管理
     */
    @Override
    public VendingMachine selectVendingMachineById(Long id)
    {
        return vendingMachineMapper.selectVendingMachineById(id);
    }

    /**
     * 查询设备管理列表
     * 
     * @param vendingMachine 设备管理
     * @return 设备管理
     */
    @Override
    public List<VendingMachine> selectVendingMachineList(VendingMachine vendingMachine)
    {
        return vendingMachineMapper.selectVendingMachineList(vendingMachine);
    }

    /**
     * 新增设备管理
     * 
     * @param vendingMachine 设备管理
     * @return 结果
     */
    @Override
    public int insertVendingMachine(VendingMachine vendingMachine)
    {
        //新增设备
        //生成8位编号，补充货道编号
        String innerCode = UUIDUtils.getUUID();
        vendingMachine.setInnerCode(innerCode); // 售货机编号
        //查询售货机类型表，补充设备容量
        VmType vmType = vmTypeService.selectVmTypeById(vendingMachine.getVmTypeId());
        vendingMachine.setChannelMaxCapacity(vmType.getChannelMaxCapacity());
        //查询点位表，补充 区域、点位、合作商等信息
        Node node = nodeService.selectNodeById(vendingMachine.getNodeId());
        BeanUtil.copyProperties(node, vendingMachine, "id");
        vendingMachine.setAddr(node.getAddress());
        //设备状态
        vendingMachine.setVmStatus(DkdContants.VM_STATUS_NODEPLOY);// 0-未投放（数据库有默认值，这个不写也不影响）
        vendingMachine.setCreateTime(DateUtils.getNowDate());// 创建时间
        vendingMachine.setUpdateTime(DateUtils.getNowDate());// 更新时间
        //保存
        int result = vendingMachineMapper.insertVendingMachine(vendingMachine);
        //新增货道
        //声明货道集合
        List<Channel> channelList = new ArrayList<>();
        //双层for循环
        for (int i = 1; i <= vmType.getVmRow(); i++) { // 外层行
            for (int j = 1; j <= vmType.getVmCol(); j++) {// 内层列
                //2-3 封装channel
                Channel channel = new Channel();
                channel.setChannelCode(i + "-" + j);// 货道编号
                channel.setVmId(vendingMachine.getId());// 售货机id
                channel.setInnerCode(vendingMachine.getInnerCode());// 售货机编号
                channel.setMaxCapacity(vmType.getChannelMaxCapacity());// 货道最大容量
                channel.setCreateTime(DateUtils.getNowDate());// 创建时间
                channel.setUpdateTime(DateUtils.getNowDate());// 更新时间
                channelList.add(channel);
            }
        }
        //批量新增
        channelService.batchInsertChannel(channelList);
        return result;
    }

    /**
     * 修改设备管理
     * 
     * @param vendingMachine 设备管理
     * @return 结果
     */
    @Override
    public int updateVendingMachine(VendingMachine vendingMachine)
    {
        //查询点位表，补充 区域、点位、合作商等信息
        Node node = nodeService.selectNodeById(vendingMachine.getNodeId());
        BeanUtil.copyProperties(node, vendingMachine, "id");// 商圈类型、区域、合作商
        vendingMachine.setAddr(node.getAddress());// 设备地址
        vendingMachine.setUpdateTime(DateUtils.getNowDate());// 更新时间
        return vendingMachineMapper.updateVendingMachine(vendingMachine);
    }

    /**
     * 批量删除设备管理
     * 
     * @param ids 需要删除的设备管理主键
     * @return 结果
     */
    @Override
    public int deleteVendingMachineByIds(Long[] ids)
    {
        return vendingMachineMapper.deleteVendingMachineByIds(ids);
    }

    /**
     * 删除设备管理信息
     * 
     * @param id 设备管理主键
     * @return 结果
     */
    @Override
    public int deleteVendingMachineById(Long id)
    {
        return vendingMachineMapper.deleteVendingMachineById(id);
    }
}
