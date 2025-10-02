import { createRouter, createWebHistory } from 'vue-router'
// 路由懒加载
const Home = () => import('./views/Home.vue')
const RecentUpload = () => import('./views/RecentUpload.vue')
const MyMedia = () => import('./views/MyMedia.vue')
const PublicMedia = () => import('./views/PublicMedia.vue')
const UploadMedia = () => import('./views/UploadMedia.vue')
const Login = () => import('./views/Login.vue')
const Register = () => import('./views/Register.vue')
const AdminPanel = () => import('./views/AdminPanel.vue')
const MediaDetail = () => import('./views/MediaDetail.vue')
const UserInfo = () => import('./views/UserInfo.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/recent-upload',
    name: 'RecentUpload',
    component: RecentUpload,
    meta: {
      requiresAuth: true,
      requiresRole: [1, 2] // 1:管理员, 2:普通用户
    }
  },
  {
    path: '/my-media',
    name: 'MyMedia',
    component: MyMedia,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/public-media',
    name: 'PublicMedia',
    component: PublicMedia,
    meta: {
      requiresAuth: true,
      requiresRole: [1, 2] // 1:管理员, 2:普通用户
    }
  },
  {
    path: '/upload',
    name: 'UploadMedia',
    component: UploadMedia,
    meta: {
      requiresAuth: true,
      requiresRole: [1, 2] // 1:管理员, 2:普通用户
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/admin',
    name: 'AdminPanel',
    component: AdminPanel,
    meta: {
      requiresAuth: true,
      requiresRole: [1] // 1:管理员
    }
  },
  {
    path: '/media/:id',
    name: 'MediaDetail',
    component: MediaDetail
  },
  {
    path: '/user-info',
    name: 'UserInfo',
    component: UserInfo,
    meta: {
      requiresAuth: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 只从sessionStorage读取用户信息，避免关闭浏览器后仍然保持登录状态
  const userInfo = sessionStorage.getItem('userInfo')
  const user = userInfo ? JSON.parse(userInfo) : null
  
  if (to.meta.requiresAuth) {
    if (!user) {
      next('/login')
    } else if (to.meta.requiresRole) {
      // 确保role类型匹配，将字符串转换为数字进行比较
      const userRole = parseInt(user.role)
      if (to.meta.requiresRole.includes(userRole)) {
        next()
      } else {
        next('/')
      }
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router