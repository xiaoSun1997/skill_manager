import { defineStore } from 'pinia'
import { login, smsLogin, logout as logoutApi, getMenus } from '@/api/auth'
import { getToken, setToken, getRefreshToken, setRefreshToken, removeToken } from '@/utils/auth'
import { usePermissionStore } from './permission'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    refreshToken: getRefreshToken(),
    userInfo: null,
    roles: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    nickname: (state) => state.userInfo?.nickname || '',
    avatar: (state) => state.userInfo?.avatar || ''
  },

  actions: {
    async login(loginData) {
      const res = await login(loginData)
      const { accessToken, refreshToken, userInfo, menus, permissions } = res.data

      this.token = accessToken
      this.refreshToken = refreshToken
      setToken(accessToken)
      setRefreshToken(refreshToken)

      this.userInfo = userInfo

      const permissionStore = usePermissionStore()
      permissionStore.setMenus(menus)
      permissionStore.setPermissions(permissions)

      return res.data
    },

    async smsLogin(smsData) {
      const res = await smsLogin(smsData)
      const { accessToken, refreshToken, userInfo, menus, permissions } = res.data

      this.token = accessToken
      this.refreshToken = refreshToken
      setToken(accessToken)
      setRefreshToken(refreshToken)

      this.userInfo = userInfo

      const permissionStore = usePermissionStore()
      permissionStore.setMenus(menus)
      permissionStore.setPermissions(permissions)

      return res.data
    },

    async getUserInfo() {
      const res = await getMenus()
      const { menus, permissions, userInfo } = res.data

      this.userInfo = userInfo

      const permissionStore = usePermissionStore()
      permissionStore.setMenus(menus)
      permissionStore.setPermissions(permissions)

      return { menus, permissions }
    },

    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // ignore
      }
      this.reset()
    },

    reset() {
      this.token = null
      this.refreshToken = null
      this.userInfo = null
      this.roles = []
      removeToken()

      const permissionStore = usePermissionStore()
      permissionStore.clear()
    }
  }
})
