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
        try {
          await userStore.getUserInfo()
          const dynamicRoutes = permissionStore.generateRoutes()
          // 添加到 Root 路由的 children 中
          const rootRoute = router.getRoutes().find(r => r.name === 'Root')
          dynamicRoutes.forEach(route => {
            if (rootRoute) {
              router.addRoute('Root', route)
            } else {
              router.addRoute(route)
            }
          })
          next({ ...to, replace: true })
        } catch (error) {
          userStore.reset()
          next(`/login?redirect=${to.path}`)
        }
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
