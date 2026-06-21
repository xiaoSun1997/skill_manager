import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    name: 'Root',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '控制台', icon: 'IconDashboard' }
      }
    ]
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/ChatPage.vue'),
    meta: { noAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { noAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
const WHITE_LIST = ['/login']

router.beforeEach(async (to, from, next) => {
  const token = getToken()

  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      const userStore = useUserStore()
      const permissionStore = usePermissionStore()

      if (permissionStore.menus.length > 0) {
        next()
      } else {
        // ── Mock mode: skip API, inject mock menus directly ──
        // 模拟管理员权限（完整菜单）
        const mockMenus = [
          {
            uuid: 'm-dashboard', name: '信息看板', type: 2, path: '/dashboard',
            component: 'dashboard/Dashboard', routeName: 'Dashboard',
            icon: 'IconDashboard', keepAlive: 1, hidden: 0, sortOrder: 0
          },
          {
            uuid: 'perm-skill-dir-000000000000001', name: '技能管理', type: 1,
            path: '/skill', icon: 'IconCode', hidden: 0, sortOrder: 5,
            children: [
              { uuid: 'perm-skill-group-menu-00001', name: '技能组管理', type: 2, path: '/skill/group',
                component: 'skill/SkillGroupList', routeName: 'SkillGroupList', icon: 'IconFolder', keepAlive: 1, hidden: 0 }
            ]
          },
          {
            uuid: 'perm-sys-dir-0000000000000001', name: '人员权限管理', type: 1,
            path: '/system', icon: 'IconSettings', hidden: 0, sortOrder: 10,
            children: [
              { uuid: 'perm-user-menu-000000000000001', name: '用户管理', type: 2, path: '/system/user',
                component: 'system/user/UserList', routeName: 'UserList', icon: 'IconUser', keepAlive: 1, hidden: 0 },
              { uuid: 'perm-role-menu-000000000000001', name: '角色管理', type: 2, path: '/system/role',
                component: 'system/role/RoleList', routeName: 'RoleList', icon: 'IconUserGroup', keepAlive: 1, hidden: 0 },
              { uuid: 'perm-perm-menu-000000000000001', name: '权限字典', type: 2, path: '/system/permission',
                component: 'system/permission/PermissionList', routeName: 'PermissionList', icon: 'IconLock', keepAlive: 1, hidden: 0 }
            ]
          },
          {
            uuid: 'm-profile', name: '个人信息', type: 2, path: '/profile',
            component: 'profile/Profile', routeName: 'Profile',
            icon: 'IconUser', keepAlive: 1, hidden: 0, sortOrder: 99
          }
        ]
        permissionStore.setMenus(mockMenus)
        permissionStore.setPermissions([
          'dashboard:view',
          'system:management', 'system:user:list', 'system:user:add', 'system:user:edit', 'system:user:delete',
          'system:role:list', 'system:role:add', 'system:role:edit', 'system:role:delete', 'system:role:assign',
          'system:permission:list', 'system:permission:add', 'system:permission:edit', 'system:permission:delete',
          'skill:management', 'skill:group:list', 'skill:group:add', 'skill:group:edit', 'skill:group:delete',
          'skill:group:file:upload', 'skill:group:file:download', 'skill:group:file:delete',
          'profile:view', 'profile:edit'
        ])
        userStore.userInfo = userStore.userInfo || {
          uuid: 'mock-uuid-001',
          username: 'admin',
          nickname: '管理员',
          avatar: ''
        }
        const dynamicRoutes = permissionStore.generateRoutes()
        const rootRoute = router.getRoutes().find(r => r.name === 'Root')
        dynamicRoutes.forEach(route => {
          if (rootRoute) router.addRoute('Root', route)
          else router.addRoute(route)
        })
        next({ ...to, replace: true })
      }
    }
  } else {
    if (WHITE_LIST.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
