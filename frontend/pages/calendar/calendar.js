const { request } = require('../../utils/request.js');

function formatDate(date) {
  const y = date.getFullYear();
  const m = `${date.getMonth() + 1}`.padStart(2, '0');
  const d = `${date.getDate()}`.padStart(2, '0');
  return `${y}-${m}-${d}`;
}

Page({
  data: {
    currentYear: 0,
    currentMonth: 0,
    allReservations: [],
    monthDays: [],
    selectedDate: '',
    selectedReservations: [],
    markedDateMap: {}
  },

  onLoad() {
    const now = new Date();
    this.setData({
      currentYear: now.getFullYear(),
      currentMonth: now.getMonth() + 1,
      selectedDate: formatDate(now)
    });
    this.loadReservations();
  },

  loadReservations() {
    request({
      url: '/student/reservation/list',
      method: 'GET'
    }).then((res) => {
      const list = res || [];
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝',
        3: '已使用',
        4: '已取消',
        5: '已失效'
      };
      const markedDateMap = {};
      list.forEach((item) => {
        item.statusText = statusMap[item.status] || '未知状态';
        if (item.reservationDate) {
          markedDateMap[item.reservationDate] = true;
        }
      });
      this.setData({
        allReservations: list,
        markedDateMap
      });
      this.generateMonthDays();
      this.updateSelectedReservations();
    }).catch((err) => {
      console.error('加载预约日历失败', err);
    });
  },

  generateMonthDays() {
    const { currentYear, currentMonth, markedDateMap, selectedDate } = this.data;
    const firstDay = new Date(currentYear, currentMonth - 1, 1);
    const firstWeekday = firstDay.getDay();
    const daysInMonth = new Date(currentYear, currentMonth, 0).getDate();
    const monthDays = [];

    for (let i = 0; i < firstWeekday; i += 1) {
      monthDays.push({
        day: '',
        date: '',
        isEmpty: true,
        hasReservation: false,
        isSelected: false
      });
    }

    for (let day = 1; day <= daysInMonth; day += 1) {
      const dateStr = formatDate(new Date(currentYear, currentMonth - 1, day));
      monthDays.push({
        day,
        date: dateStr,
        isEmpty: false,
        hasReservation: !!markedDateMap[dateStr],
        isSelected: selectedDate === dateStr
      });
    }

    this.setData({ monthDays });
  },

  updateSelectedReservations() {
    const { allReservations, selectedDate } = this.data;
    const selectedReservations = allReservations.filter(
      (item) => item.reservationDate === selectedDate
    );
    this.setData({ selectedReservations });
  },

  selectDate(e) {
    const date = e.currentTarget.dataset.date;
    if (!date) {
      return;
    }
    this.setData({ selectedDate: date });
    this.generateMonthDays();
    this.updateSelectedReservations();
  },

  changeMonth(e) {
    const step = Number(e.currentTarget.dataset.step || 0);
    let { currentYear, currentMonth } = this.data;
    currentMonth += step;
    if (currentMonth < 1) {
      currentMonth = 12;
      currentYear -= 1;
    } else if (currentMonth > 12) {
      currentMonth = 1;
      currentYear += 1;
    }
    this.setData({
      currentYear,
      currentMonth
    });
    this.generateMonthDays();
  },

  navToDetail(e) {
    const item = e.currentTarget.dataset.item;
    if (!item) {
      return;
    }
    const dataStr = encodeURIComponent(JSON.stringify(item));
    wx.navigateTo({
      url: `/pages/reservationDetail/reservationDetail?data=${dataStr}`
    });
  }
});
