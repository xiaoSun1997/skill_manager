import { usePermissionStore } from '@/store/permission'

export default {
  mounted(el, binding) {
    const { value } = binding
    const permissionStore = usePermissionStore()
    const permissions = permissionStore.permissions

    if (value && value instanceof Array && value.length > 0) {
      const hasPermission = permissions.some(permission => value.includes(permission))
      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else if (value && typeof value === 'string') {
      if (!permissions.includes(value)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}
