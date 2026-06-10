console.log('=== equipment-borrow.js v2026052732 已加载 ===');
const API_BASE = '/api/equipment-borrow';
let allRecords = [];
let equipmentList = []; // 设备清单

document.addEventListener('DOMContentLoaded', function() {
  loadRecords();
  loadStats();
  setupTabs();
});

function setupTabs() {
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', function() {
      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
      this.classList.add('active');
      document.getElementById('tab-' + this.dataset.tab).classList.add('active');
    });
  });
}

function loadRecords() {
  const status = document.getElementById('filterStatus').value;
  let url = API_BASE;
  if (status) url += '?status=' + encodeURIComponent(status);

  fetch(url)
    .then(res => res.json())
    .then(data => {
      allRecords = data;
      renderAllRecords(data);
      renderPendingRecords();
      renderPendingReturnRecords();
      renderBorrowedRecords();
      renderReturnedRecords();
      renderOverdueRecords();
    })
    .catch(err => console.error('加载记录失败:', err));
}

function loadStats() {
  Promise.all([
    fetch(API_BASE + '/overdue').then(r => r.json()),
    fetch(API_BASE + '/due-soon').then(r => r.json()),
    fetch(API_BASE + '?status=已借出').then(r => r.json()),
    fetch(API_BASE + '?status=待导师审批').then(r => r.json()),
    fetch(API_BASE + '?status=待管理员审批').then(r => r.json())
  ]).then(([overdue, dueSoon, borrowed, pendingMentor, pendingAdmin]) => {
    const totalPending = pendingMentor.length + pendingAdmin.length;
    document.getElementById('statOverdue').textContent = overdue.length;
    document.getElementById('statDueSoon').textContent = dueSoon.length;
    document.getElementById('statBorrowed').textContent = borrowed.length;
    document.getElementById('statPending').textContent = totalPending;

    // 显示逾期告警
    const overdueArea = document.getElementById('overdueAlertArea');
    if (overdue.length > 0) {
      overdueArea.innerHTML = '<div class="alert-box alert-danger">⚠️ 有 <strong>' + overdue.length + '</strong> 台设备已逾期，请及时处理！</div>';
    } else {
      overdueArea.innerHTML = '';
    }

    // 显示即将到期提醒
    const dueSoonArea = document.getElementById('dueSoonArea');
    if (dueSoon.length > 0) {
      dueSoonArea.innerHTML = '<div class="alert-box alert-warning">🔔 有 <strong>' + dueSoon.length + '</strong> 台设备明天到期，请提醒借用人归还。</div>';
    } else {
      dueSoonArea.innerHTML = '';
    }
  });
}

function renderAllRecords(records) {
  const tbody = document.getElementById('recordsTable');
  if (records.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="9">暂无记录</td></tr>';
    return;
  }
  tbody.innerHTML = records.map(r => `
    <tr>
      <td>${r.recordNo}</td>
      <td>${r.equipmentAssetNo || '-'}</td>
      <td>${r.equipmentName || '-'}</td>
      <td>${r.borrowerName || '-'}</td>
      <td>${r.purpose || '-'}</td>
      <td>${r.expectedReturnDate}</td>
      <td>${r.actualReturnDate || '-'}</td>
      <td>${getStatusBadge(r)}</td>
      <td>${getActionButtons(r)}</td>
    </tr>
  `).join('');
}

function renderPendingRecords() {
  // 显示待导师审批、待管理员审批的记录，以及续借待审批的记录
  const pending = allRecords.filter(r =>
    r.status === '待导师审批' || r.status === '待管理员审批' || r.renewalStatus === '续借待审批'
  );
  const tbody = document.getElementById('pendingTable');
  if (pending.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="9">暂无待审批记录</td></tr>';
    return;
  }
  tbody.innerHTML = pending.map(r => {
    let approvalInfo = '';
    let actionBtns = '';
    if (r.status === '待导师审批') {
      approvalInfo = '<span style="color:#e67e22;">待导师审批</span>' + (r.mentorName ? '（导师：' + r.mentorName + '）' : '');
      actionBtns = '<button class="btn btn-sm btn-success" onclick="showApproveModal(' + r.id + ')">审批</button>';
    } else if (r.status === '待管理员审批') {
      approvalInfo = '<span style="color:#3498db;">待管理员审批</span>' + (r.mentorName ? '（导师已批：' + r.mentorName + '）' : '');
      actionBtns = '<button class="btn btn-sm btn-success" onclick="showApproveModal(' + r.id + ')">审批</button>';
    } else if (r.renewalStatus === '续借待审批') {
      approvalInfo = '<span style="color:#8b5cf6;">续借待审批</span><br><span style="font-size:12px;color:#666;">原应还：' + r.expectedReturnDate + ' → 申请改为：' + r.renewalNewReturnDate + '</span>';
      actionBtns = '<button class="btn btn-sm btn-success" onclick="showRenewalApproveModal(' + r.id + ')">审批</button>';
    }
    return `
    <tr>
      <td>${r.recordNo}</td>
      <td>${r.equipmentName || '-'}</td>
      <td>${r.borrowerName || '-'}</td>
      <td>${r.purpose || '-'}</td>
      <td>${r.expectedReturnDate}</td>
      <td>${approvalInfo}</td>
      <td>${r.renewalStatus === '续借待审批' ? (r.renewalRemark || '-') : (r.remark || '-')}</td>
      <td>${actionBtns}</td>
    </tr>
  `}).join('');
}

function renderPendingReturnRecords() {
  const pendingReturn = allRecords.filter(r => r.status === '待验收');
  const tbody = document.getElementById('pendingReturnTable');
  if (pendingReturn.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="7">暂无待验收记录</td></tr>';
    return;
  }
  tbody.innerHTML = pendingReturn.map(r => `
    <tr>
      <td>${r.recordNo}</td>
      <td>${r.equipmentName || '-'}</td>
      <td>${r.borrowerName || '-'}</td>
      <td>${r.expectedReturnTime || '-'}</td>
      <td>${r.expectedReturnLocation || '-'}</td>
      <td>${r.returnApplyTime || '-'}</td>
      <td>
        <button class="btn btn-sm btn-primary" onclick="showReturnModal(${r.id})">验收</button>
      </td>
    </tr>
  `).join('');
}

function renderBorrowedRecords() {
  const borrowed = allRecords.filter(r => r.status === '已借出');
  const tbody = document.getElementById('borrowedTable');
  if (borrowed.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="7">暂无借出记录</td></tr>';
    return;
  }
  tbody.innerHTML = borrowed.map(r => `
    <tr>
      <td>${r.recordNo}</td>
      <td>${r.equipmentName || '-'}</td>
      <td>${r.borrowerName || '-'}</td>
      <td>${r.purpose || '-'}</td>
      <td>${r.expectedReturnDate}</td>
      <td>${r.overdue ? '<span style="color:#dc2626;font-weight:600">' + r.overdueDays + '天</span>' : '-'}</td>
      <td>
        <button class="btn btn-sm btn-success" onclick="returnEquipment(${r.id})">归还</button>
        <button class="btn btn-sm btn-warning" onclick="showRenewalModal(${r.id})">续借</button>
      </td>
    </tr>
  `).join('');
}

function renderReturnedRecords() {
  const returned = allRecords.filter(r => r.status === '已归还');
  const tbody = document.getElementById('returnedTable');
  if (returned.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="8">暂无归还记录</td></tr>';
    return;
  }
  tbody.innerHTML = returned.map(r => {
    let resultBadge = '-';
    if (r.returnResult === '完好') {
      resultBadge = '<span style="color:#16a34a;font-weight:600">✅ 完好</span>';
    } else if (r.returnResult === '损坏') {
      resultBadge = '<span style="color:#dc2626;font-weight:600">❌ 损坏</span>';
    } else if (r.returnResult === '缺件') {
      resultBadge = '<span style="color:#f59e0b;font-weight:600">⚠️ 缺件</span>';
    }
    return `
    <tr>
      <td>${r.recordNo}</td>
      <td>${r.equipmentName || '-'}</td>
      <td>${r.borrowerName || '-'}</td>
      <td>${r.purpose || '-'}</td>
      <td>${r.expectedReturnDate}</td>
      <td>${r.actualReturnDate}</td>
      <td>${resultBadge}</td>
      <td>${r.verifierName || '-'}</td>
    </tr>
  `}).join('');
}

function renderOverdueRecords() {
  const overdue = allRecords.filter(r => r.overdue);
  const tbody = document.getElementById('overdueTable');
  const reminderArea = document.getElementById('overdueReminderArea');

  if (overdue.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="9">暂无逾期记录</td></tr>';
    reminderArea.innerHTML = '';
    return;
  }

  // 显示逾期提醒
  reminderArea.innerHTML = '<div class="alert-box alert-danger">⚠️ 共有 <strong>' + overdue.length + '</strong> 台设备已逾期，请及时通知借用人归还！</div>';

  tbody.innerHTML = overdue.map(r => {
    let reminderMsg = '';
    if (r.overdueDays <= 3) {
      reminderMsg = '<span style="color:#f59e0b;">🔔 请尽快归还</span>';
    } else if (r.overdueDays <= 7) {
      reminderMsg = '<span style="color:#dc2626;">⚠️ 已逾期' + r.overdueDays + '天，请立即归还</span>';
    } else {
      reminderMsg = '<span style="color:#dc2626;font-weight:600;">🚨 严重逾期' + r.overdueDays + '天，请联系管理员</span>';
    }
    let warningBadge = r.overdueWarning ? '<br><span style="color:#dc2626;font-size:11px;">⚠️ 已告警(' + r.overdueWarningTime + ')</span>' : '';
    return `
    <tr>
      <td>${r.recordNo}</td>
      <td>${r.equipmentName || '-'}</td>
      <td>${r.borrowerName || '-'}</td>
      <td>${r.phone || '-'}</td>
      <td>${r.expectedReturnDate}</td>
      <td><span style="color:#dc2626;font-weight:600">${r.overdueDays}天</span>${warningBadge}</td>
      <td>${reminderMsg}</td>
      <td>
        <button class="btn btn-sm btn-danger" onclick="showOverdueWarningModal(${r.id})">⚠️ 告警</button>
        <button class="btn btn-sm btn-success" onclick="returnEquipment(${r.id})">归还</button>
        <button class="btn btn-sm btn-warning" onclick="showRenewalModal(${r.id})">续借</button>
      </td>
    </tr>
  `}).join('');
}

function getStatusBadge(r) {
  const map = {
    '待导师审批': 'status-pending',
    '待管理员审批': 'status-pending',
    '已批准': 'status-approved',
    '已借出': r.overdue ? 'status-overdue' : 'status-borrowed',
    '已归还': 'status-returned',
    '已拒绝': 'status-rejected',
    '待验收': 'status-pending'
  };
  return '<span class="status-badge ' + (map[r.status] || '') + '">' + r.status + '</span>';
}

function getActionButtons(r) {
  let btns = '';
  if (r.status === '待导师审批' || r.status === '待管理员审批') {
    btns += '<button class="btn btn-sm btn-success" onclick="showApproveModal(' + r.id + ')">审批</button>';
  } else if (r.status === '已批准') {
    btns += '<button class="btn btn-sm btn-primary" onclick="pickupEquipment(' + r.id + ')">领取</button>';
  } else if (r.status === '已借出') {
    btns += '<button class="btn btn-sm btn-success" onclick="returnEquipment(' + r.id + ')">归还</button> ';
    btns += '<button class="btn btn-sm btn-warning" onclick="showRenewalModal(' + r.id + ')">续借</button>';
  } else if (r.status === '待验收') {
    btns += '<button class="btn btn-sm btn-primary" onclick="showReturnModal(' + r.id + ')">验收</button>';
  }
  return btns;
}

function showBorrowModal() {
  console.log('showBorrowModal called');
  const modal = document.getElementById('borrowModal');
  console.log('Modal element:', modal);
  modal.classList.add('active');
  
  const filterSection = document.getElementById('filterSection');
  console.log('Filter section:', filterSection);
  console.log('Filter section display:', filterSection ? filterSection.style.display : 'not found');
  
  loadCategories();
  loadEquipmentNames();
  loadFilteredEquipment();
  loadUserOptions();
  loadMentorOptions();
  
  // 设置默认借用日期为今天
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('borrowDate').value = today;
}

// 加载设备类别
function loadCategories() {
  fetch('/api/equipment-categories')
    .then(res => res.json())
    .then(data => {
      const sel = document.getElementById('filterCategory');
      sel.innerHTML = '<option value="">全部类别</option>' + 
        data.map(c => '<option value="' + c.id + '">' + c.name + '</option>').join('');
    });
}

// 加载设备名称下拉框
function loadEquipmentNames() {
  fetch('/api/equipment?status=在库-可用')
    .then(res => res.json())
    .then(data => {
      const sel = document.getElementById('filterEquipmentName');
      sel.innerHTML = '<option value="">全部设备</option>' + 
        data.map(e => '<option value="' + e.id + '">' + e.name + ' (' + e.assetNo + ')</option>').join('');
    });
}

// 加载筛选后的设备列表
function loadFilteredEquipment() {
  const equipmentId = document.getElementById('filterEquipmentName').value;
  const categoryId = document.getElementById('filterCategory').value;
  const statusSelect = document.getElementById('filterBorrowStatus');
  const status = (statusSelect && statusSelect.value) ? statusSelect.value : '在库-可用';
  
  console.log('加载设备筛选 - 设备ID:', equipmentId, '类别:', categoryId, '状态:', status);
  
  let url = '/api/equipment?status=' + encodeURIComponent(status);
  if (equipmentId) url += '&id=' + equipmentId;
  if (categoryId) url += '&categoryId=' + categoryId;
  
  console.log('请求URL:', url);
  
  fetch(url)
    .then(res => res.json())
    .then(data => {
      console.log('获取到设备数据:', data);
      const sel = document.getElementById('borrowEquipment');
      if (data.length === 0) {
        sel.innerHTML = '<option value="">暂无可用设备</option>';
      } else {
        sel.innerHTML = '<option value="">请选择设备</option>' + 
          data.map(e => {
            const importantTag = e.isImportant ? ' [重要设备]' : '';
            return '<option value="' + e.id + '">' + e.name + ' (' + e.assetNo + ') - ' + e.status + importantTag + '</option>';
          }).join('');
      }
    })
    .catch(err => {
      console.error('加载设备失败:', err);
    });
}

// 当选择设备时，显示设备详细信息
function onEquipmentSelect() {
  const equipmentId = document.getElementById('borrowEquipment').value;
  const infoDiv = document.getElementById('selectedEquipmentInfo');
  
  if (!equipmentId) {
    infoDiv.style.display = 'none';
    return;
  }
  
  fetch('/api/equipment/' + equipmentId)
    .then(res => res.json())
    .then(equipment => {
      infoDiv.innerHTML = `
        <strong>设备信息：</strong><br/>
        资产编号：${equipment.assetNo}<br/>
        型号：${equipment.model || '-'}<br/>
        分类：${equipment.categoryName || '-'}<br/>
        存放位置：${equipment.locationName || '-'}<br/>
        责任人：${equipment.responsibleName || '-'}<br/>
        ${equipment.isImportant ? '<span style="color:#dc2626;font-weight:600;">⚠️ 重要设备 - 需要导师审批</span>' : ''}
      `;
      infoDiv.style.display = 'block';
      
      // 选择设备后重新判断是否需要导师审批
      onBorrowerChange();
    });
}

// 添加设备到清单（保留设备选择功能，但清单改为文本输入）
function addEquipmentToList() {
  const equipmentId = document.getElementById('borrowEquipment').value;
  if (!equipmentId) {
    alert('请先选择设备');
    return;
  }
  
  fetch('/api/equipment/' + equipmentId)
    .then(res => res.json())
    .then(equipment => {
      // 将设备名称添加到文本输入框
      const textarea = document.getElementById('equipmentListText');
      const currentText = textarea.value.trim();
      if (currentText) {
        textarea.value = currentText + '、' + equipment.name;
      } else {
        textarea.value = equipment.name;
      }
      
      // 清空选择
      document.getElementById('borrowEquipment').value = '';
      document.getElementById('selectedEquipmentInfo').style.display = 'none';
      
      // 重新判断是否需要导师审批
      onBorrowerChange();
    });
}

// 加载用户选项
function loadUserOptions() {
  console.log('=== 开始加载用户选项 ===');
  fetch('/api/users')
    .then(res => res.json())
    .then(data => {
      console.log('获取到用户数据:', data);
      const sel = document.getElementById('borrowUser');
      sel.innerHTML = '<option value="">请选择借用人</option>' + 
        data.map(u => {
          // 根据用户名前缀判断角色
          const roleTag = u.username.startsWith('stu_') ? ' [学生]' : 
                         u.username.startsWith('tea_') ? ' [教师]' : '';
          const role = u.username.startsWith('stu_') ? 'STUDENT' : 'TEACHER';
          const option = '<option value="' + u.id + '" data-role="' + role + '">' + u.realName + ' (' + u.username + ')' + roleTag + '</option>';
          console.log('生成用户选项:', option);
          return option;
        }).join('');
      console.log('用户选项加载完成，HTML:', sel.innerHTML);
    })
    .catch(err => {
      console.error('加载用户选项失败:', err);
    });
}

// 加载导师选项（只加载导师角色，过滤学生）
function loadMentorOptions() {
  fetch('/api/users')
    .then(res => res.json())
    .then(data => {
      console.log('加载导师选项，所有用户:', data);
      // 过滤出导师（用户名包含 teacher 或 lab_，排除 stu_ 和 admin）
      const mentors = data.filter(u => 
        (u.username.includes('teacher') || u.username.startsWith('lab_')) && 
        !u.username.startsWith('stu_') && 
        !u.username.startsWith('admin')
      );
      console.log('过滤后的导师:', mentors);
      const sel = document.getElementById('borrowMentor');
      if (mentors && mentors.length > 0) {
        sel.innerHTML = '<option value="">请选择导师</option>' + 
          mentors.map(m => '<option value="' + m.id + '">' + m.realName + ' (' + m.username + ')</option>').join('');
      } else {
        sel.innerHTML = '<option value="">暂无导师可选</option>';
      }
    })
    .catch(err => {
      console.error('加载导师选项失败:', err);
      const sel = document.getElementById('borrowMentor');
      sel.innerHTML = '<option value="">加载失败</option>';
    });
}

// 当借用人改变时，判断是否需要导师审批
function onBorrowerChange() {
  const borrowerId = document.getElementById('borrowUser').value;
  const mentorSelect = document.getElementById('borrowMentor');
  const mentorTip = document.getElementById('mentorTip');
  
  // 检查是否是学生（通过 data-role 属性判断）
  const selectedUserOption = document.querySelector('#borrowUser option[value="' + borrowerId + '"]');
  const isStudent = borrowerId && selectedUserOption && selectedUserOption.dataset && selectedUserOption.dataset.role === 'STUDENT';
  
  // 只有学生借用设备时才需要导师审批
  if (isStudent) {
    mentorSelect.disabled = false;
    mentorSelect.required = true;
    mentorTip.style.display = 'block';
  } else {
    // 管理员和老师不需要导师审批，下拉框变灰色
    mentorSelect.disabled = true;
    mentorSelect.value = '';
    mentorSelect.required = false;
    mentorTip.style.display = 'none';
  }
}

function closeModal(id) {
  document.getElementById(id).classList.remove('active');
  
  // 关闭借出弹窗时清空设备清单文本
  if (id === 'borrowModal') {
    document.getElementById('equipmentListText').value = '';
  }
}

function submitBorrow() {
  const equipmentId = document.getElementById('borrowEquipment').value;
  const borrowerId = document.getElementById('borrowUser').value;
  const purpose = document.getElementById('borrowPurpose').value;
  const expectedReturnDate = document.getElementById('borrowReturnDate').value;
  const remark = document.getElementById('borrowRemark').value;
  const phone = document.getElementById('borrowPhone').value;
  const useLocation = document.getElementById('borrowUseLocation').value;
  const mentorId = document.getElementById('borrowMentor').value;
  const equipmentListText = document.getElementById('equipmentListText').value;

  if (!equipmentId || !borrowerId || !purpose || !expectedReturnDate) {
    alert('请填写必填字段');
    return;
  }

  const data = { equipmentId, borrowerId, purpose, expectedReturnDate, remark, phone, useLocation };
  if (mentorId) {
    data.mentorId = mentorId;
  }
  // 将设备清单文本附加到备注中
  if (equipmentListText) {
    data.remark = (remark ? remark + '\n' : '') + '设备清单：' + equipmentListText;
  }

  fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  .then(res => res.json())
  .then(data => {
    alert('申请提交成功！记录编号：' + data.recordNo);
    closeModal('borrowModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('提交失败：' + err));
}

function showApproveModal(id) {
  document.getElementById('approveRecordId').value = id;
  document.getElementById('approveRemark').value = '';
  document.getElementById('approveModifySection').style.display = 'none';
  document.getElementById('approveModalTitle').textContent = '审批借出申请';

  // 获取记录详情，判断审批阶段
  fetch(API_BASE + '/' + id)
    .then(res => res.json())
    .then(record => {
      if (record.status === '待导师审批') {
        document.getElementById('approveModalTitle').textContent = '导师审批';
        document.getElementById('approveModifySection').style.display = 'none';
      } else if (record.status === '待管理员审批') {
        document.getElementById('approveModalTitle').textContent = '管理员审批';
        document.getElementById('approveModifySection').style.display = 'block';
        document.getElementById('approveReturnDate').value = record.expectedReturnDate;
      }
      document.getElementById('approveModal').classList.add('active');
    })
    .catch(err => {
      document.getElementById('approveModal').classList.add('active');
    });
}

function approveRecord() {
  const id = document.getElementById('approveRecordId').value;
  const approveRemark = document.getElementById('approveRemark').value;
  const approverId = 3; // 管理员ID

  // 如果是管理员审批，检查是否有修改应还日期
  const newReturnDate = document.getElementById('approveReturnDate').value;
  if (newReturnDate && document.getElementById('approveModifySection').style.display !== 'none') {
    // 先更新应还日期
    fetch(API_BASE + '/' + id + '/update-return-date', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ newReturnDate })
    })
    .then(res => res.json())
    .then(() => {
      // 再执行审批
      doApprove(id, approverId, approveRemark);
    })
    .catch(err => alert('修改日期失败：' + err));
  } else {
    doApprove(id, approverId, approveRemark);
  }
}

function doApprove(id, approverId, approveRemark) {
  fetch(API_BASE + '/' + id + '/approve', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ approverId, approveRemark })
  })
  .then(res => res.json())
  .then(data => {
    if (data.status === '待管理员审批') {
      alert('导师审批通过，已转交管理员审批！');
    } else if (data.status === '已批准') {
      alert('审批通过！库存已锁定。');
    } else {
      alert('审批通过！');
    }
    closeModal('approveModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('审批失败：' + err));
}

function rejectRecord() {
  const id = document.getElementById('approveRecordId').value;
  const approveRemark = document.getElementById('approveRemark').value;
  const approverId = 3;

  fetch(API_BASE + '/' + id + '/reject', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ approverId, approveRemark })
  })
  .then(res => res.json())
  .then(data => {
    alert('已拒绝申请');
    closeModal('approveModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('操作失败：' + err));
}

function pickupEquipment(id) {
  showPickupModal(id);
}

function showPickupModal(id) {
  const record = allRecords.find(r => r.id === id);
  if (!record) {
    alert('记录不存在');
    return;
  }

  document.getElementById('pickupRecordId').value = id;
  document.getElementById('pickupDeviceInfo').textContent = record.equipmentName + ' (' + record.equipmentAssetNo + ')';
  document.getElementById('pickupRemark').value = '';
  document.getElementById('pickupEquipmentStatus').value = '完好';
  
  // 设置领取时间为今天
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('pickupDate').value = today;
  
  // 加载领取人选项
  loadPickupPersonOptions();
  
  document.getElementById('pickupModal').classList.add('active');
}

function loadPickupPersonOptions() {
  fetch('/api/users')
    .then(res => res.json())
    .then(data => {
      const sel = document.getElementById('pickupPerson');
      sel.innerHTML = '<option value="">请选择领取人</option>' + 
        data.map(u => '<option value="' + u.id + '">' + u.realName + ' (' + u.username + ')</option>').join('');
    })
    .catch(err => console.error('加载用户列表失败:', err));
}

function confirmPickup() {
  const id = document.getElementById('pickupRecordId').value;
  const pickupPersonId = document.getElementById('pickupPerson').value;
  const pickupRemark = document.getElementById('pickupRemark').value;
  
  if (!pickupPersonId) {
    alert('请选择领取人');
    return;
  }
  
  fetch(API_BASE + '/' + id + '/pickup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      pickupPersonId: pickupPersonId,
      pickupRemark: pickupRemark
    })
  })
  .then(res => res.json())
  .then(data => {
    alert('领取成功！');
    closeModal('pickupModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('操作失败：' + err));
}

function returnEquipment(id) {
  showReturnApplyModal(id);
}

function showReturnApplyModal(id) {
  const record = allRecords.find(r => r.id === id);
  if (!record) {
    alert('记录不存在');
    return;
  }

  document.getElementById('returnApplyRecordId').value = id;
  document.getElementById('returnApplyDeviceInfo').textContent = record.equipmentName + ' (' + record.equipmentAssetNo + ') - 借用人：' + record.borrowerName;
  
  // 设置默认归还时间为当前时间
  const now = new Date();
  const defaultTime = now.toISOString().slice(0, 16);
  document.getElementById('returnApplyTime').value = defaultTime;
  document.getElementById('returnApplyLocation').value = '';
  
  document.getElementById('returnApplyModal').classList.add('active');
}

function submitReturnApply() {
  const id = document.getElementById('returnApplyRecordId').value;
  const expectedReturnTime = document.getElementById('returnApplyTime').value;
  const expectedReturnLocation = document.getElementById('returnApplyLocation').value;

  if (!expectedReturnTime) {
    alert('请选择预计归还时间');
    return;
  }
  if (!expectedReturnLocation || expectedReturnLocation.trim() === '') {
    alert('请填写预计归还地点');
    return;
  }

  fetch(API_BASE + '/' + id + '/apply-return', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      expectedReturnTime: expectedReturnTime,
      expectedReturnLocation: expectedReturnLocation
    })
  })
  .then(res => res.json())
  .then(data => {
    alert('归还申请已提交！请等待管理员验收。');
    closeModal('returnApplyModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('操作失败：' + err));
}

function showReturnModal(id) {
  // 找到记录信息
  const record = allRecords.find(r => r.id === id);
  if (!record) {
    alert('记录不存在');
    return;
  }

  document.getElementById('returnRecordId').value = id;
  document.getElementById('returnDeviceInfo').textContent = record.equipmentName + ' (' + record.equipmentAssetNo + ') - 借用人：' + record.borrowerName;
  document.getElementById('returnResult').value = '';
  document.getElementById('returnLocation').value = '';
  document.getElementById('returnAccessories').value = '';
  document.getElementById('returnDamageDesc').value = '';
  document.getElementById('damageDescGroup').style.display = 'none';
  
  // 加载验收人选项
  loadReturnVerifierOptions();
  
  document.getElementById('returnModal').classList.add('active');
}

function loadReturnVerifierOptions() {
  fetch('/api/users')
    .then(res => res.json())
    .then(data => {
      const sel = document.getElementById('returnVerifier');
      sel.innerHTML = '<option value="">请选择验收人</option>' + 
        data.map(u => '<option value="' + u.id + '">' + u.realName + ' (' + u.username + ')</option>').join('');
    })
    .catch(err => console.error('加载用户列表失败:', err));
}

function toggleDamageFields() {
  const result = document.getElementById('returnResult').value;
  const damageGroup = document.getElementById('damageDescGroup');
  if (result === '损坏' || result === '缺件') {
    damageGroup.style.display = 'block';
  } else {
    damageGroup.style.display = 'none';
  }
}

function submitReturn() {
  const id = document.getElementById('returnRecordId').value;
  const returnResult = document.getElementById('returnResult').value;
  const returnLocation = document.getElementById('returnLocation').value;
  const accessoriesInfo = document.getElementById('returnAccessories').value;
  const damageDescription = document.getElementById('returnDamageDesc').value;
  const verifierId = document.getElementById('returnVerifier').value;

  if (!verifierId) {
    alert('请选择验收人');
    return;
  }
  if (!returnResult) {
    alert('请选择验收结果');
    return;
  }

  if ((returnResult === '损坏' || returnResult === '缺件') && !damageDescription) {
    alert('损坏或缺件时必须填写说明');
    return;
  }

  fetch(API_BASE + '/' + id + '/return', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      verifierId: verifierId,
      returnResult: returnResult,
      returnLocation: returnLocation,
      accessoriesInfo: accessoriesInfo,
      damageDescription: damageDescription
    })
  })
  .then(res => {
    if (!res.ok) {
      return res.json().then(err => { throw new Error(err.message || '归还失败'); });
    }
    return res.json();
  })
  .then(data => {
    let msg = '归还成功！';
    if (data.returnResult === '损坏') {
      msg += '\n设备已标记为"在库-待维修"，请及时处理。';
    } else if (data.returnResult === '缺件') {
      msg += '\n设备已标记为"配件不全"，请及时处理。';
    }
    alert(msg);
    closeModal('returnModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('归还失败：' + err.message));
}

function showRenewalModal(id) {
  document.getElementById('renewalRecordId').value = id;
  document.getElementById('renewalModal').classList.add('active');
}

function submitRenewal() {
  const id = document.getElementById('renewalRecordId').value;
  const newReturnDate = document.getElementById('renewalDate').value;
  const remark = document.getElementById('renewalRemark').value;

  if (!newReturnDate) {
    alert('请选择新的归还日期');
    return;
  }

  fetch(API_BASE + '/' + id + '/renewal', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ newReturnDate, remark })
  })
  .then(res => res.json())
  .then(data => {
    alert('续借申请已提交！');
    closeModal('renewalModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('续借失败：' + err));
}

// ===== 续借审批 =====
function showRenewalApproveModal(id) {
  const record = allRecords.find(r => r.id === id);
  if (!record) {
    alert('记录不存在');
    return;
  }

  document.getElementById('renewalApproveRecordId').value = id;
  document.getElementById('renewalApproveDevice').textContent = record.equipmentName + ' (' + record.equipmentAssetNo + ')';
  document.getElementById('renewalApproveBorrower').textContent = record.borrowerName;
  document.getElementById('renewalApproveDateRange').textContent = record.expectedReturnDate + ' → ' + record.renewalNewReturnDate;
  document.getElementById('renewalApproveRemark').textContent = record.renewalRemark || '无';
  document.getElementById('renewalApproveRemarkInput').value = '';
  document.getElementById('renewalApproveModal').classList.add('active');
}

function approveRenewal() {
  const id = document.getElementById('renewalApproveRecordId').value;
  const approveRemark = document.getElementById('renewalApproveRemarkInput').value;

  fetch(API_BASE + '/' + id + '/approve-renewal', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      approverId: 3,
      approved: true,
      approveRemark: approveRemark
    })
  })
  .then(res => res.json())
  .then(data => {
    alert('续借已批准！应还日期已更新为：' + data.renewalNewReturnDate);
    closeModal('renewalApproveModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('操作失败：' + err));
}

function rejectRenewal() {
  const id = document.getElementById('renewalApproveRecordId').value;
  const approveRemark = document.getElementById('renewalApproveRemarkInput').value;

  if (!approveRemark) {
    alert('拒绝续借必须填写审批意见');
    return;
  }

  fetch(API_BASE + '/' + id + '/approve-renewal', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      approverId: 3,
      approved: false,
      approveRemark: approveRemark
    })
  })
  .then(res => res.json())
  .then(data => {
    alert('续借已拒绝');
    closeModal('renewalApproveModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('操作失败：' + err));
}

// ===== 逾期告警 =====
function showOverdueWarningModal(id) {
  const record = allRecords.find(r => r.id === id);
  if (!record) {
    alert('记录不存在');
    return;
  }

  document.getElementById('overdueWarningRecordId').value = id;
  document.getElementById('overdueWarningDevice').textContent = record.equipmentName + ' (' + record.equipmentAssetNo + ')';
  document.getElementById('overdueWarningBorrower').textContent = record.borrowerName;
  document.getElementById('overdueWarningDays').textContent = record.overdueDays + '天';
  document.getElementById('overdueWarningRemarkInput').value = '';
  document.getElementById('overdueWarningModal').classList.add('active');
}

function submitOverdueWarning() {
  const id = document.getElementById('overdueWarningRecordId').value;
  const warningRemark = document.getElementById('overdueWarningRemarkInput').value;

  if (!warningRemark) {
    alert('请填写告警备注');
    return;
  }

  fetch(API_BASE + '/' + id + '/overdue-warning', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ warningRemark: warningRemark })
  })
  .then(res => res.json())
  .then(data => {
    alert('告警已记录！');
    closeModal('overdueWarningModal');
    loadRecords();
    loadStats();
  })
  .catch(err => alert('操作失败：' + err));
}