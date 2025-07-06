import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
    return request({
        url: '/user/list',
        method: 'get',
        params
    })
}

// 删除用户
export function deleteUser(userId) {
    return request({
        url: `/user/${userId}`,
        method: 'delete'
    })
}