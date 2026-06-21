import { defineStore } from 'pinia'

// 组件映射：后端返回的 component 路径 -> 实际组件
const componentMap = {
  'system/user/UserList': () => import('@/views/system/user/UserList.vue'),
  'system/role/RoleList': () => import('@/views/system/role/RoleList.vue'),
  'system/permission/PermissionList': () => import('@/views/system/permission/PermissionList.vue'),
  'dashboard/Dashboard': () => import('@/views/dashboard/Dashboard.vue'),
  'skill/SkillList': () => import('@/views/skill/SkillList.vue'),
  'skill/SkillGroupList': () => import('@/views/skill/SkillGroupList.vue'),
  'skill/FileManager': () => import('@/views/skill/FileManager.vue'),
  'profile/Profile': () => import('@/views/profile/Profile.vue'),
  'settings/ModelConfig': () => import('@/views/settings/ModelConfig.vue')
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    menus: [],
    permissions: []
  }),

  getters: {
    hasPermission: (state) => (code) => {
      return state.permissions.includes(code)
    },

    sidebarMenus: (state) => {
      // 只返回 type=1(目录) 和 type=2(菜单) 且 hidden=0 的
      return state.menus.filter(m => m.hidden !== 1)
    }
  },

  actions: {
    setMenus(menus) {
      this.menus = menus || []
    },

    setPermissions(permissions) {
      this.permissions = permissions || []
    },

    generateRoutes() {
      const routes = []
      this.menus.forEach(menu => {
        const route = this.buildRoute(menu)
        if (route) {
          routes.push(route)
        }
      })
      return routes
    },

    buildRoute(menu) {
      if (menu.type === 1) {
        // 目录 - 创建父路由
        const children = (menu.children || [])
          .map(child => this.buildRoute(child))
          .filter(Boolean)

        if (children.length === 0) return null

        return {
          path: menu.path || '/' + menu.routeName?.toLowerCase(),
          component: { template: '<router-view />' },
          redirect: children[0]?.path,
          meta: {
            title: menu.name,
            icon: menu.icon,
            uuid: menu.uuid
          },
          children
        }
      } else if (menu.type === 2) {
        // 菜单 - 叶子路由
        const componentLoader = componentMap[menu.component]
        if (!componentLoader) return null

        return {
          path: menu.path,
          name: menu.routeName,
          component: componentLoader,
          meta: {
            title: menu.name,
            icon: menu.icon,
            uuid: menu.uuid,
            keepAlive: menu.keepAlive === 1
          }
        }
      }
      return null
    },

    clear() {
      this.menus = []
      this.permissions = []
    }
  }
})
